package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import io.swagger.v3.oas.annotations.Hidden
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.GoalPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionScheduleRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewScheduleRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventPublisher
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.PrisonerReceivedIntoPrisonEventService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.PrisonerSearchApiService
import java.time.Instant
import java.time.LocalDate

private val log = KotlinLogging.logger {}

/**
 * Temporary controller class exposing REST API method that triggers the ETL of ReviewSchedule and InductionSchedule records
 */
@Hidden
@RestController
class ScheduleEtlController(
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val reviewScheduleRepository: ReviewScheduleRepository,
  private val inductionScheduleRepository: InductionScheduleRepository,
  private val eventPublisher: EventPublisher,
  private val inductionRepository: InductionRepository,
  private val goalPersistenceAdapter: GoalPersistenceAdapter,
  private val reviewScheduleService: ReviewScheduleService,
  private val prisonerReceivedIntoPrisonEventService: PrisonerReceivedIntoPrisonEventService,
) {
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @GetMapping(value = ["/action-plans/schedules/etl-check/{prisonId}"])
  @Transactional
  fun checkSchedulesForPrisonersInPrison(
    @PathVariable("prisonId") prisonId: String,
  ): CheckSchedulesEtlResponse {
    log.info("Check ETL process for prison ID: $prisonId")

    val allPrisoners = prisonerSearchApiService.getAllPrisonersInPrison(prisonId).also {
      log.info("Total prisoners in prison $prisonId: ${it.size}")
    }

    val prisonersWithSchedules = prisonersWithAnySchedule(allPrisoners)

    // filter out the prisoners with either a review schedule or an induction schedule
    val allPrisonersWithoutSchedules = allPrisoners.map { it.prisonerNumber }
      .filterNot { it in prisonersWithSchedules }

    // Prepare response data
    val response = CheckSchedulesEtlResponse(
      prisonId = prisonId,
      totalNumberOfPrisoners = allPrisoners.size,
      prisonersWithoutPLPData = allPrisonersWithoutSchedules,
    )

    log.info("ETL check process completed for prison ID: $prisonId. Response: ${response.prisonersWithoutPLPData.size}")
    return response
  }

  data class PrisonNumbersRequest(
    val prisonNumbers: List<String>,
  )

  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_VIEW_REVIEWS)
  @GetMapping("/release-dates/{prisonId}")
  fun releaseDatesByPrisonId(
    @PathVariable prisonId: String,
  ): ReleaseDates {
    log.info("Getting release dates for prison ID: $prisonId")

    val allPrisoners = prisonerSearchApiService.getAllPrisonersInPrison(prisonId).also {
      log.info("Total prisoners in prison $prisonId: ${it.size}")
    }

    val tomorrow = LocalDate.now().plusDays(1)

    // we are only interested in prisoners with no release date or ones with the date < tomorrow
    val prisonerReleaseDates: List<PrisonerReleaseDate> = allPrisoners.mapNotNull { prisoner ->
      val date = prisoner.releaseDate
      if (date == null || date.isBefore(tomorrow)) {
        PrisonerReleaseDate(
          prisoner.prisonerNumber,
          date,
          hasCompletedInduction(prisoner.prisonerNumber),
          getCurrentReviewScheduleStatus(prisoner.prisonerNumber),
        )
      } else {
        null
      }
    }

    val releaseDates = ReleaseDates(prisonerReleaseDates)
    log.info(
      "Release date check completed for prison ID: $prisonId. " +
        "Returning ${releaseDates.prisonerReleaseDates.size} result(s).",
    )
    return releaseDates
  }

  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @PostMapping("/release-dates/{prisonId}/review-schedules")
  fun correctReviewSchedules(
    @PathVariable prisonId: String,
  ) {
    log.info("Getting release dates for prison ID: $prisonId")

    val allPrisoners = prisonerSearchApiService.getAllPrisonersInPrison(prisonId)

    // we are only interested in prisoners with no release date or ones with the date < tomorrow
    val prisonerReleaseDates: List<PrisonerReleaseDate> = allPrisoners.mapNotNull { prisoner ->
      val date = prisoner.releaseDate
      if (date != null && date.isBefore(LocalDate.now())) {
        PrisonerReleaseDate(
          prisoner.prisonerNumber,
          date,
          hasCompletedInduction(prisoner.prisonerNumber),
          getCurrentReviewScheduleStatus(prisoner.prisonerNumber),
        )
      } else {
        null
      }
    }

    // only process people who have a completed induction,
    // have no current review schedule and their release date is in the past.

    prisonerReleaseDates.forEach {
      if (it.completedInduction && it.currentReviewStatus == null) {
        log.debug("Creating review schedule for prisoner: ${it.prisonerNumber} at prison $prisonId")
        prisonerReceivedIntoPrisonEventService.processPrisonerAdmissionEvent(nomsNumber = it.prisonerNumber, eventOccurredAt = Instant.now())
        Thread.sleep(500) // have a rest between each one so it doesn't kill the database
      }
    }
  }

  private fun getCurrentReviewScheduleStatus(prisonerNumber: String): String? {
    val review = reviewScheduleRepository.findFirstByPrisonNumberOrderByUpdatedAtDesc(prisonerNumber)
    return review?.scheduleStatus?.name
  }

  // returns true if the person has a completed induction and goal
  private fun hasCompletedInduction(prisonerNumber: String): Boolean {
    val induction = inductionRepository.findByPrisonNumber(prisonerNumber)
    val goals = goalPersistenceAdapter.getGoals(prisonerNumber)
    return induction != null && !goals.isNullOrEmpty()
  }

  data class ReleaseDates(val prisonerReleaseDates: List<PrisonerReleaseDate>)
  data class PrisonerReleaseDate(
    val prisonerNumber: String,
    val releaseDate: LocalDate?,
    val completedInduction: Boolean,
    val currentReviewStatus: String?,
  )

  @PostMapping("/action-plans/schedules/publish-review-messages")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @Transactional
  fun generateReviewMessages(
    @RequestBody prisonNumbersRequest: PrisonNumbersRequest,
  ) {
    val reviewPrisonNumbers = prisonNumbersRequest.prisonNumbers.distinct()
    reviewPrisonNumbers.forEach(eventPublisher::createAndPublishReviewScheduleEvent)
  }

  @PostMapping("/action-plans/schedules/publish-induction-messages")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @Transactional
  fun generateInductionMessages(
    @RequestBody prisonNumbersRequest: PrisonNumbersRequest,
  ) {
    val inductionPrisonNumbers = prisonNumbersRequest.prisonNumbers.distinct()
    inductionPrisonNumbers.forEach(eventPublisher::createAndPublishInductionEvent)
  }

  private fun prisonersWithAnySchedule(prisoners: List<Prisoner>): List<String> {
    val prisonNumbers = prisoners.map { it.prisonerNumber }
    val prisonersWithReviewSchedules =
      reviewScheduleRepository.findByPrisonNumberIn(prisonNumbers).map { it.prisonNumber }.toSet()
    val prisonersWithInductionSchedules =
      inductionScheduleRepository.findByPrisonNumberIn(prisonNumbers).map { it.prisonNumber }.toSet()
    return (prisonersWithReviewSchedules + prisonersWithInductionSchedules).toSet().toList()
  }
}

data class CheckSchedulesEtlResponse(
  val prisonId: String,
  val prisonersWithoutPLPData: List<String> = listOf(),
  val totalNumberOfPrisoners: Int,
) {

  val summary: String
    get() =
      (
        """
          Prison ID: $prisonId
          Total number of prisoners: $totalNumberOfPrisoners
          Number of prisoners with no PLP schedule: ${prisonersWithoutPLPData.size}
          Prison IDs: $prisonersWithoutPLPData 
        """.trimIndent()
        )
}
