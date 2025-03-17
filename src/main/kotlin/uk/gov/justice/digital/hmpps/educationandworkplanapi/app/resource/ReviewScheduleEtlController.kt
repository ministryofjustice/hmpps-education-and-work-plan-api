package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import io.swagger.v3.oas.annotations.Hidden
import mu.KotlinLogging
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
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ActiveReviewScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNoReleaseDateForSentenceTypeException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewScheduleRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.CreateInitialReviewScheduleMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.PrisonerSearchApiService

private val log = KotlinLogging.logger {}

/**
 * Temporary controller class exposing REST API method that triggers the ETL of ReviewSchedule records
 */
@Hidden
@RestController
class ReviewScheduleEtlController(
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val inductionRepository: InductionRepository,
  private val actionPlanRepository: ActionPlanRepository,
  private val reviewScheduleRepository: ReviewScheduleRepository,
  private val reviewScheduleService: ReviewScheduleService,
  private val createInitialReviewScheduleMapper: CreateInitialReviewScheduleMapper,
) {

  @PostMapping(value = ["/action-plans/review-schedules/etl/{prisonId}"])
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @Transactional
  fun createReviewSchedulesForPrisonersInPrison(
    @PathVariable("prisonId") prisonId: String,
    @RequestParam(required = false, name = "dryRun") dryRun: Boolean = false,
  ): ReviewSchedulesEtlResponse {
    // Create a ReviewSchedule for all prisoners in the given prison who need one

    val totalPrisonersInPrison: Int
    val totalPrisonersWithReviewSchedule: Int
    val totalPrisonersWithInduction: Int
    val totalPrisonersWithActionPlan: Int
    val prisonersWithCreatedReviewSchedules = mutableListOf<String>()
    val prisonersWithoutReviewSchedules = mutableListOf<String>()

    // Get all prisoners in the given prison
    prisonerSearchApiService.getAllPrisonersInPrison(prisonId)
      .also { totalPrisonersInPrison = it.size }
      // Filter the prisoners, keeping only those who do not already have a Review Schedule, and who have both an Induction and Action Plan with at least 1 goal
      // Get those without any Review Schedule (most DB efficient is a direct call to the repository findByPrisonNumberIn method)
      .let { prisoners ->
        val prisonNumbers = prisoners.map { it.prisonerNumber }
        val prisonersWithReviewSchedule = reviewScheduleRepository.findByPrisonNumberIn(prisonNumbers)
          .map { it.prisonNumber }
          .toSet()
          .also { totalPrisonersWithReviewSchedule = it.size }
        prisoners.filter { !prisonersWithReviewSchedule.contains(it.prisonerNumber) }
      }
      // Of those, get those who have an Induction (most DB efficient is a direct call to the repository findByPrisonNumberIn method)
      .let { prisoners ->
        val prisonNumbers = prisoners.map { it.prisonerNumber }
        val prisonersWithInduction = inductionRepository.findByPrisonNumberIn(prisonNumbers)
          .map { it.prisonNumber }
          .also { totalPrisonersWithInduction = it.size }
        prisoners.filter { prisonersWithInduction.contains(it.prisonerNumber) }
      }
      // Of those who have an induction, get just those who have an Action Plan with at least 1 goal (most DB efficient is a direct call to the repository findByPrisonNumberIn method)
      .let { prisoners ->
        val prisonNumbers = prisoners.map { it.prisonerNumber }
        val prisonersWithAnActionPlan = actionPlanRepository.findByPrisonNumberIn(prisonNumbers)
          .map { it.prisonNumber }
          .also { totalPrisonersWithActionPlan = it.size }
        prisoners.filter { prisonersWithAnActionPlan.contains(it.prisonerNumber) }
      }
      // Create the Review Schedule for each prisoner who requires one
      .forEach { prisoner ->
        val prisonNumber = prisoner.prisonerNumber
        // When retrospectively creating a ReviewSchedule (as we are here) we won't be able to tell if the prisoner entered their current prison because of a readmission or transfer
        val createInitialReviewScheduleDto = createInitialReviewScheduleMapper.fromPrisonerToDomain(
          prisoner = prisoner,
          isReadmission = false,
          isTransfer = false,
        )

        log.debug { "Creating Review Schedule for $prisonNumber" }

        try {
          reviewScheduleService.createInitialReviewSchedule(createInitialReviewScheduleDto)
            ?.run { prisonersWithCreatedReviewSchedules.add(prisonNumber) }
            ?: run { prisonersWithoutReviewSchedules.add(prisonNumber) }
        } catch (e: Exception) {
          when (e) {
            is NullPointerException -> log.error { "NPE creating Review Schedule for $prisonNumber, likely a problem with missing releaseDate. $e" }
            is ReviewScheduleNoReleaseDateForSentenceTypeException, is ActiveReviewScheduleAlreadyExistsException -> { /* ignore, these are expected exceptions given the prisoner data in certain circumstances (SentenceType, releaseDate) */ }

            else -> log.error { "Failed to create Review Schedule for $prisonNumber. $e" }
          }
          prisonersWithoutReviewSchedules.add(prisonNumber)
        }
      }

    val responseData = ReviewSchedulesEtlResponse(
      dryRun = dryRun,
      prisonId = prisonId,
      totalPrisonersInPrison = totalPrisonersInPrison,
      totalPrisonersWithReviewSchedule = totalPrisonersWithReviewSchedule,
      totalPrisonersWithInduction = totalPrisonersWithInduction,
      totalPrisonersWithActionPlan = totalPrisonersWithActionPlan,
      prisonersWithCreatedReviewSchedules = prisonersWithCreatedReviewSchedules.toList(),
      prisonersWithoutReviewSchedules = prisonersWithoutReviewSchedules.toList(),
    )

    if (!dryRun) {
      return responseData
    } else {
      // This was a dry run so we need to throw an exception to rollback the transaction and therefore not persist any data
      throw ReviewSchedulesEtlRollbackException(responseData)
    }
  }

  @ExceptionHandler(value = [ReviewSchedulesEtlRollbackException::class])
  fun handleReviewSchedulesEtlRollbackException(e: ReviewSchedulesEtlRollbackException): ResponseEntity<Any> = ResponseEntity
    .status(HttpStatus.OK)
    .body(e.reviewSchedulesEtlResponse)
}

data class ReviewSchedulesEtlResponse(
  val dryRun: Boolean,
  val prisonId: String,
  val totalPrisonersInPrison: Int,
  val totalPrisonersWithReviewSchedule: Int,
  val totalPrisonersWithInduction: Int,
  val totalPrisonersWithActionPlan: Int,
  val prisonersWithCreatedReviewSchedules: List<String>,
  val prisonersWithoutReviewSchedules: List<String>,
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
          Prisoners requiring a Review Schedule
          -------------------------------------
          Total of $totalPrisonersInPrison prisoners in $prisonId.
          Of those, $totalPrisonersWithReviewSchedule already have a Review Schedule, leaving ${totalPrisonersInPrison - totalPrisonersWithReviewSchedule} candidate prisoners.
          Of those, $totalPrisonersWithInduction have an Induction, and $totalPrisonersWithActionPlan have an Induction and an Action Plan.
          The $totalPrisonersWithActionPlan prisoners with both an Induction and an Action Plan are the candidate prisoners.
          
          Created Review Schedules
          ------------------------          
          Review Schedules for ${prisonersWithoutReviewSchedules.size} prisoners were not created:
          $prisonersWithoutReviewSchedules
          
          Review Schedules for ${prisonersWithCreatedReviewSchedules.size} prisoners were successfully created:
          $prisonersWithCreatedReviewSchedules
        """.trimIndent()
}

data class ReviewSchedulesEtlRollbackException(
  val reviewSchedulesEtlResponse: ReviewSchedulesEtlResponse,
) : RuntimeException()
