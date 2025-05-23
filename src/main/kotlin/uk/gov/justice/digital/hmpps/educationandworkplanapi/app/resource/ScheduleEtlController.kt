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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionScheduleRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewScheduleRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventPublisher
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.PrisonerSearchApiService

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

  @PostMapping("/action-plans/schedules/publish-review-messages")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @Transactional
  fun generateMessages(
    @RequestBody prisonNumbersRequest: PrisonNumbersRequest,
  ) {
    val reviewPrisonNumbers = prisonNumbersRequest.prisonNumbers.distinct()
    reviewPrisonNumbers.forEach(eventPublisher::createAndPublishReviewScheduleEvent)
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
