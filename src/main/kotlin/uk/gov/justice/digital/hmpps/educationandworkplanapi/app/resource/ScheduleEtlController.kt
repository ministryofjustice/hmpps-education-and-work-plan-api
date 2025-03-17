package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import io.swagger.v3.oas.annotations.Hidden
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ActiveReviewScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNoReleaseDateForSentenceTypeException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionScheduleRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewScheduleRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventPublisher
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.CreateInitialReviewScheduleMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.PrisonerSearchApiService
import java.time.LocalDate

private val log = KotlinLogging.logger {}

/**
 * Temporary controller class exposing REST API method that triggers the ETL of ReviewSchedule and InductionSchedule records
 */
@Hidden
@RestController
class ScheduleEtlController(
  @Value("\${EDUCATION_CONTRACTS_START_DATE:}") private val goLiveDate: LocalDate? = null,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val inductionRepository: InductionRepository,
  private val actionPlanRepository: ActionPlanRepository,
  private val reviewScheduleRepository: ReviewScheduleRepository,
  private val inductionScheduleRepository: InductionScheduleRepository,
  private val inductionScheduleService: InductionScheduleService,
  private val reviewScheduleService: ReviewScheduleService,
  private val createInitialReviewScheduleMapper: CreateInitialReviewScheduleMapper,
  private val eventPublisher: EventPublisher,
) {

  @PostMapping("/action-plans/schedules/etl-messages")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @Transactional
  fun generateMessages(
    @RequestParam(name = "dryRun", required = false, defaultValue = "false") dryRun: Boolean,
  ): MessagesEtlResponse {
    val inductionPrisonNumbers = inductionScheduleRepository.findAll().map { it.prisonNumber }
    val reviewPrisonNumbers = reviewScheduleRepository.findAll().map { it.prisonNumber }.toSet().toList()

    if (!dryRun) {
      inductionPrisonNumbers.forEach(eventPublisher::createAndPublishInductionEvent)
      reviewPrisonNumbers.forEach(eventPublisher::createAndPublishReviewScheduleEvent)
    }

    return MessagesEtlResponse(
      dryRun = dryRun,
      inductionSchedulePrisonerIds = inductionPrisonNumbers,
      reviewSchedulePrisonerIds = reviewPrisonNumbers,
    ).also {
      log.info(
        """
            ETL Published messages:
            Dry run: ${it.dryRun}
            Number of induction messages: ${it.numberOfInductionScheduleMessages}
            Number of review messages: ${it.numberOfReviewScheduleMessages}
        """.trimIndent(),
      )
    }
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @PostMapping(value = ["/action-plans/schedules/etl/{prisonId}"])
  @Transactional
  fun createSchedulesForPrisonersInPrison(
    @PathVariable("prisonId") prisonId: String,
    @RequestParam(required = false, name = "dryRun") dryRun: Boolean = false,
  ): SchedulesEtlResponse {
    log.info("Starting ETL process for prison ID: $prisonId, dryRun: $dryRun")

    val allPrisoners = prisonerSearchApiService.getAllPrisonersInPrison(prisonId).also {
      log.info("Total prisoners in prison $prisonId: ${it.size}")
    }

    val totalPrisonersInPrison = allPrisoners.size

    // Get filtered prisoners step-by-step
    val prisonersWithoutReviewSchedules = filterPrisonersWithoutReviewSchedules(allPrisoners)
    val prisonersWithoutInductionSchedule = filterPrisonersWithoutInductionSchedule(allPrisoners)
    val prisonersWithInductions = filterPrisonersWithInductions(allPrisoners)
    val prisonersWithInductionsAndNoReviewSchedule =
      filterPrisonersWithInductionButNoReviewSchedule(prisonersWithInductions, prisonersWithoutReviewSchedules)

    val eligibleInductionSchedulePrisoners =
      filterPrisonersWithNoInduction(prisonersWithoutInductionSchedule, prisonersWithInductions)
    val eligibleReviewSchedulePrisoners = filterPrisonersWithActionPlans(prisonersWithInductionsAndNoReviewSchedule)

    val totalPrisonersWithInductionSchedule = totalPrisonersInPrison - prisonersWithoutInductionSchedule.size
    val totalPrisonersWithReviewSchedule = totalPrisonersInPrison - prisonersWithoutReviewSchedules.size
    val totalPrisonersWithActionPlan = eligibleReviewSchedulePrisoners.size

    // Create schedules for eligible prisoners
    val createdReviewSchedules = mutableListOf<String>()
    val failedReviewSchedules = mutableListOf<String>()
    val createdInductionSchedules = mutableListOf<String>()
    val failedInductionSchedules = mutableListOf<String>()

    createInductionSchedules(eligibleInductionSchedulePrisoners, createdInductionSchedules, failedInductionSchedules)
    createReviewSchedules(eligibleReviewSchedulePrisoners, createdReviewSchedules, failedReviewSchedules)

    // Prepare response data
    val response = SchedulesEtlResponse(
      dryRun = dryRun,
      prisonId = prisonId,
      totalPrisonersInPrison = totalPrisonersInPrison,
      totalPrisonersWithReviewSchedule = totalPrisonersWithReviewSchedule,
      totalPrisonersWithInductionSchedule = totalPrisonersWithInductionSchedule,
      totalPrisonersWithInduction = prisonersWithInductions.size,
      totalPrisonersWithActionPlan = totalPrisonersWithActionPlan,
      eligibleInductionSchedulePrisoners = eligibleInductionSchedulePrisoners.size,
      eligibleReviewSchedulePrisoners = eligibleReviewSchedulePrisoners.size,
      prisonersWithCreatedReviewSchedules = createdReviewSchedules,
      prisonersWithoutReviewSchedules = failedReviewSchedules,
      prisonersWithCreatedInductionSchedule = createdInductionSchedules,
      prisonersWithoutInductionSchedule = failedInductionSchedules,
    )

    // Handle dry run
    if (dryRun) {
      log.info("Dry run completed. Response: ${response.summary}")
      throw SchedulesEtlRollbackException(response)
    }

    log.info("ETL process completed for prison ID: $prisonId. Response: ${response.summary}")
    return response
  }

  private fun createReviewSchedules(
    eligibleReviewSchedulePrisoners: List<Prisoner>,
    createdReviewSchedules: MutableList<String>,
    failedReviewSchedules: MutableList<String>,
  ) {
    eligibleReviewSchedulePrisoners.forEach { prisoner ->
      val prisonNumber = prisoner.prisonerNumber
      try {
        createReviewSchedule(prisoner)
        createdReviewSchedules.add(prisonNumber)
      } catch (e: Exception) {
        handleReviewScheduleCreationError(prisonNumber, e, failedReviewSchedules)
      }
    }
  }

  private fun createInductionSchedules(
    eligibleInductionSchedulePrisoners: List<Prisoner>,
    createdInductionSchedules: MutableList<String>,
    failedInductionSchedules: MutableList<String>,
  ) {
    eligibleInductionSchedulePrisoners.forEach { prisoner ->
      if (prisoner.releaseDate != null &&
        prisoner.releaseDate.isBefore(goLiveDate().plusDays(7))
      ) {
        log.info { "Induction for prisoner ${prisoner.prisonerNumber} skipped due to release within 7 days of go-live." }
      } else {
        val prisonNumber = prisoner.prisonerNumber
        try {
          createInductionSchedule(prisoner)
          createdInductionSchedules.add(prisonNumber)
        } catch (e: Exception) {
          handleInductionScheduleCreationError(prisonNumber, e, failedInductionSchedules)
        }
      }
    }
  }

  /**
   * Returns go live date - this is configured in the specific values yaml or default to today.
   */
  protected fun goLiveDate(): LocalDate {
    val today = LocalDate.now()
    return if (goLiveDate != null && goLiveDate.isAfter(today)) {
      goLiveDate
    } else {
      today
    }
  }

  private fun filterPrisonersWithNoInduction(
    prisoners: List<Prisoner>,
    prisonersWithInductions: List<Prisoner>,
  ): List<Prisoner> {
    val prisonerNumbersWithInductions = prisonersWithInductions.map { it.prisonerNumber }.toSet()
    return prisoners.filter { it.prisonerNumber !in prisonerNumbersWithInductions }
  }

  private fun filterPrisonersWithoutReviewSchedules(prisoners: List<Prisoner>): List<Prisoner> {
    val prisonNumbers = prisoners.map { it.prisonerNumber }
    val prisonersWithReviewSchedules =
      reviewScheduleRepository.findByPrisonNumberIn(prisonNumbers).map { it.prisonNumber }.toSet()
    return prisoners.filter { it.prisonerNumber !in prisonersWithReviewSchedules }
  }

  private fun filterPrisonersWithoutInductionSchedule(prisoners: List<Prisoner>): List<Prisoner> {
    val prisonNumbers = prisoners.map { it.prisonerNumber }
    val prisonersWithInductionSchedules =
      inductionScheduleRepository.findByPrisonNumberIn(prisonNumbers).map { it.prisonNumber }.toSet()
    return prisoners.filter { it.prisonerNumber !in prisonersWithInductionSchedules }
  }

  private fun filterPrisonersWithInductions(prisoners: List<Prisoner>): List<Prisoner> {
    val prisonNumbers = prisoners.map { it.prisonerNumber }
    val prisonersWithInductions =
      inductionRepository.findByPrisonNumberIn(prisonNumbers).map { it.prisonNumber }.toSet()
    return prisoners.filter { it.prisonerNumber in prisonersWithInductions }
  }

  private fun filterPrisonersWithInductionButNoReviewSchedule(
    prisonersWithInduction: List<Prisoner>,
    prisonersWithoutReviewSchedules: List<Prisoner>,
  ): List<Prisoner> {
    val prisonNumbersWithNoReviewSchedule = prisonersWithoutReviewSchedules.map { it.prisonerNumber }
    return prisonersWithInduction.filter { it.prisonerNumber in prisonNumbersWithNoReviewSchedule }
  }

  private fun filterPrisonersWithActionPlans(prisoners: List<Prisoner>): List<Prisoner> {
    val prisonNumbers = prisoners.map { it.prisonerNumber }
    val prisonersWithActionPlans =
      actionPlanRepository.findByPrisonNumberIn(prisonNumbers).map { it.prisonNumber }.toSet()
    return prisoners.filter { it.prisonerNumber in prisonersWithActionPlans }
  }

  private fun createReviewSchedule(prisoner: Prisoner) {
    val createInitialReviewScheduleDto = createInitialReviewScheduleMapper.fromPrisonerToDomain(
      prisoner = prisoner,
      isReadmission = false,
      isTransfer = false,
    )
    reviewScheduleService.createInitialReviewSchedule(createInitialReviewScheduleDto)
  }

  private fun createInductionSchedule(prisoner: Prisoner) {
    // Needs to create an induction schedule of with a deadline 6 months out
    inductionScheduleService.createInductionSchedule(
      prisonNumber = prisoner.prisonerNumber,
      prisonerAdmissionDate = LocalDate.now(),
      prisonId = prisoner.prisonId ?: "N/A",
      newAdmission = false,
      releaseDate = prisoner.releaseDate,
    )
  }

  private fun handleReviewScheduleCreationError(prisonNumber: String, e: Exception, failedList: MutableList<String>) {
    when (e) {
      is NullPointerException -> log.error("NPE creating Review Schedule for $prisonNumber: Missing releaseDate. $e")
      is ReviewScheduleNoReleaseDateForSentenceTypeException, is ActiveReviewScheduleAlreadyExistsException -> log.warn(
        "Expected exception for $prisonNumber: $e",
      )

      else -> log.error("Unexpected error creating Review Schedule for $prisonNumber: $e")
    }
    failedList.add(prisonNumber)
  }

  private fun handleInductionScheduleCreationError(
    prisonNumber: String,
    e: Exception,
    failedList: MutableList<String>,
  ) {
    when (e) {
      is NullPointerException -> log.error("NPE creating Induction Schedule for $prisonNumber $e")
      is InductionScheduleAlreadyExistsException -> log.warn(
        "Expected exception for $prisonNumber: $e",
      )

      else -> log.error("Unexpected error creating Induction Schedule for $prisonNumber: $e")
    }
    failedList.add(prisonNumber)
  }

  @ExceptionHandler(SchedulesEtlRollbackException::class)
  fun handleRollbackException(e: SchedulesEtlRollbackException): ResponseEntity<SchedulesEtlResponse> {
    log.info("Dry run rollback triggered. Response: ${e.schedulesEtlResponse.summary}")
    return ResponseEntity.ok(e.schedulesEtlResponse)
  }
}

data class MessagesEtlResponse(
  val dryRun: Boolean,
  val reviewSchedulePrisonerIds: List<String> = listOf(),
  val inductionSchedulePrisonerIds: List<String> = listOf(),
) {
  val numberOfReviewScheduleMessages: Int
    get() = reviewSchedulePrisonerIds.size
  val numberOfInductionScheduleMessages: Int
    get() = inductionSchedulePrisonerIds.size
}

data class SchedulesEtlResponse(
  val dryRun: Boolean,
  val prisonId: String,
  val totalPrisonersInPrison: Int,
  val totalPrisonersWithInductionSchedule: Int,
  val totalPrisonersWithReviewSchedule: Int,
  val totalPrisonersWithInduction: Int,
  val totalPrisonersWithActionPlan: Int,
  val prisonersWithCreatedReviewSchedules: List<String>,
  val prisonersWithoutReviewSchedules: List<String>,
  val prisonersWithCreatedInductionSchedule: List<String>,
  val prisonersWithoutInductionSchedule: List<String>,
  val eligibleInductionSchedulePrisoners: Int,
  val eligibleReviewSchedulePrisoners: Int,
) {
  val summary: String
    get() =
      (
        if (dryRun) {
          """
            ***************
            *** DRY RUN ***
            ***************
    
          """.trimIndent()
        } else {
          ""
        }
        ) +
        """
          Prisoners requiring Schedules
          -------------------------------------
          Total of $totalPrisonersInPrison prisoners in $prisonId.
          Of those, $totalPrisonersWithInductionSchedule already have an Induction Schedule, 
          leaving ${totalPrisonersInPrison - totalPrisonersWithInductionSchedule} candidates for induction schedules.
          Of those, $totalPrisonersWithInduction have an Induction, 
          leaving $eligibleInductionSchedulePrisoners eligible for induction schedule creation.
          
          Total of $totalPrisonersInPrison prisoners in $prisonId.
          Of those, $totalPrisonersWithReviewSchedule already have a Review Schedule, leaving ${totalPrisonersInPrison - totalPrisonersWithReviewSchedule} candidate prisoners.
          Of those, $totalPrisonersWithInduction have an Induction, and $totalPrisonersWithActionPlan have an Induction and an Action Plan.
          leaving $eligibleReviewSchedulePrisoners eligible for review schedule creation.
          
          Created Review Schedules
          ------------------------          
          Review Schedules for ${prisonersWithoutReviewSchedules.size} prisoners were not created:
          $prisonersWithoutReviewSchedules
          
          Review Schedules for ${prisonersWithCreatedReviewSchedules.size} prisoners were successfully created:
          $prisonersWithCreatedReviewSchedules
          
          Created Induction Schedule
          ------------------------          
          Induction Schedule for ${prisonersWithoutInductionSchedule.size} prisoners were not created:
          $prisonersWithoutInductionSchedule
          
          Induction Schedules for ${prisonersWithCreatedInductionSchedule.size} prisoners were successfully created:
          $prisonersWithCreatedInductionSchedule
        """.trimIndent()
}

data class SchedulesEtlRollbackException(
  val schedulesEtlResponse: SchedulesEtlResponse,
) : RuntimeException()
