package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ActiveReviewScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleHistory
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNoReleaseDateForSentenceTypeException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.UpdatedReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateInitialReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.UpdateReviewScheduleStatusDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.effectiveSentenceType
import java.time.LocalDate

private val log = KotlinLogging.logger {}

class ReviewScheduleService(
  private val reviewSchedulePersistenceAdapter: ReviewSchedulePersistenceAdapter,
  private val reviewScheduleEventService: ReviewScheduleEventService,
) {

  private val reviewScheduleDateCalculationService = ReviewScheduleDateCalculationService()

  private val reviewScheduleStatusTransitionValidator = ReviewScheduleStatusTransitionValidator()

  /**
   * Returns the active [ReviewSchedule] for the prisoner identified by their prison number, where "active" is defined
   * as not having the status "COMPLETED".
   * Otherwise, throws [ReviewScheduleNotFoundException] if it cannot be found.
   */
  fun getActiveReviewScheduleForPrisoner(prisonNumber: String): ReviewSchedule =
    reviewSchedulePersistenceAdapter.getActiveReviewSchedule(prisonNumber) ?: throw ReviewScheduleNotFoundException(
      prisonNumber,
    )

  /**
   * Returns the latest [ReviewSchedule] for the prisoner identified by their prison number. The latest (most recently
   * updated) [ReviewSchedule] is returned irrespective of status.
   * Otherwise, throws [ReviewScheduleNotFoundException] if it cannot be found.
   */
  fun getLatestReviewScheduleForPrisoner(prisonNumber: String): ReviewSchedule =
    reviewSchedulePersistenceAdapter.getLatestReviewSchedule(prisonNumber) ?: throw ReviewScheduleNotFoundException(
      prisonNumber,
    )

  /**
   * Returns a list of [ReviewSchedule] for the prisoner identified by their prison number.
   */
  fun getReviewSchedulesForPrisoner(prisonNumber: String): List<ReviewScheduleHistory> {
    val responses = reviewSchedulePersistenceAdapter
      .getReviewScheduleHistory(prisonNumber)

    return responses.sortedWith(
      compareByDescending<ReviewScheduleHistory> { it.lastUpdatedAt }
        .thenByDescending { it.version },
    )
  }

  /**
   * Creates and returns the prisoner's initial [ReviewSchedule].
   * Returns null if the prisoner has less than 3 months to serve, in which case no Review is necessary and one is not
   * scheduled for them.
   *
   * To create a new [ReviewSchedule] the prisoner is required to have a release date for most sentence types. The
   * exception to this are sentence types REMAND, CONVICTED_UNSENTENCED and INDETERMINATE_SENTENCE. All other sentence
   * types require a release date. Throws a [ReviewScheduleNoReleaseDateForSentenceTypeException] if this condition
   * is not satisfied.
   */
  fun createInitialReviewSchedule(createInitialReviewScheduleDto: CreateInitialReviewScheduleDto): ReviewSchedule? =
    with(createInitialReviewScheduleDto) {
      val sentenceType = effectiveSentenceType(prisonerSentenceType, prisonerHasIndeterminateFlag, prisonerHasRecallFlag)

      // Check for an existing active review schedule
      val existingReviewSchedule = runCatching {
        getActiveReviewScheduleForPrisoner(prisonNumber)
      }.getOrNull()

      if (existingReviewSchedule != null) {
        throw ActiveReviewScheduleAlreadyExistsException(prisonNumber)
      }

      // Calculate the review schedule window and rule
      val releaseDate = prisonerReleaseDate
      val reviewScheduleCalculationRule = reviewScheduleDateCalculationService.determineReviewScheduleCalculationRule(
        prisonNumber = prisonNumber,
        sentenceType = sentenceType,
        releaseDate = releaseDate,
        isReAdmission = isReadmission,
        isTransfer = isTransfer,
      )
      // Persist the initial review schedule if a ReviewScheduleWindow is calculated
      reviewScheduleDateCalculationService.calculateReviewWindow(reviewScheduleCalculationRule, releaseDate)
        ?.let {
          reviewSchedulePersistenceAdapter.createReviewSchedule(
            CreateReviewScheduleDto(
              prisonNumber = prisonNumber,
              prisonId = prisonId,
              reviewScheduleWindow = it,
              scheduleCalculationRule = reviewScheduleCalculationRule,
            ),
          )
        }
    }

  fun updateLatestReviewScheduleStatus(
    prisonNumber: String,
    prisonId: String,
    newStatus: ReviewScheduleStatus,
    exemptionReason: String?,
  ) {
    val reviewSchedule = reviewSchedulePersistenceAdapter.getLatestReviewSchedule(prisonNumber)
      ?: throw ReviewScheduleNotFoundException(prisonNumber)

    // Validate the status transition
    reviewScheduleStatusTransitionValidator.validate(prisonNumber, reviewSchedule.scheduleStatus, newStatus)

    when {
      newStatus == ReviewScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE -> {
        updateReviewScheduleFollowingSystemTechnicalIssue(reviewSchedule, exemptionReason, prisonId, prisonNumber)
      }

      newStatus.isExemptionOrExclusion() -> {
        updateExemptStatus(reviewSchedule, newStatus, exemptionReason, prisonId, prisonNumber)
      }

      else -> {
        updateScheduledStatus(reviewSchedule, prisonId, prisonNumber)
      }
    }
  }

  /**
   * Updates the prisoner's active Review Schedule by setting its status to EXEMPT_PRISONER_RELEASE
   *
   * The prisoner's active Review Schedule is the one with the status SCHEDULED or one of the EXEMPT_ statuses.
   * A Review Schedule with the status COMPLETE is not considered 'active'
   */
  fun exemptActiveReviewScheduleStatusDueToPrisonerRelease(prisonNumber: String, prisonId: String) =
    reviewSchedulePersistenceAdapter.getActiveReviewSchedule(prisonNumber)
      ?.run {
        updateExemptStatus(
          prisonNumber = prisonNumber,
          prisonId = prisonId,
          reviewSchedule = this,
          newStatus = ReviewScheduleStatus.EXEMPT_PRISONER_RELEASE,
          exemptionReason = null,
        ).also {
          log.debug { "Review Schedule for prisoner [$prisonNumber] set to exempt: EXEMPT_PRISONER_RELEASE" }
        }
      }
      ?: throw ReviewScheduleNotFoundException(prisonNumber)

  /**
   * Updates the prisoner's active Review Schedule by setting its status to EXEMPT_PRISONER_DEATH
   *
   * The prisoner's active Review Schedule is the one with the status SCHEDULED or one of the EXEMPT_ statuses.
   * A Review Schedule with the status COMPLETE is not considered 'active'
   */
  fun exemptActiveReviewScheduleStatusDueToPrisonerDeath(prisonNumber: String, prisonId: String) =
    reviewSchedulePersistenceAdapter.getActiveReviewSchedule(prisonNumber)
      ?.run {
        updateExemptStatus(
          prisonNumber = prisonNumber,
          prisonId = prisonId,
          reviewSchedule = this,
          newStatus = ReviewScheduleStatus.EXEMPT_PRISONER_DEATH,
          exemptionReason = null,
        ).also {
          log.debug { "Review Schedule for prisoner [$prisonNumber] set to exempt: EXEMPT_PRISONER_DEATH" }
        }
      }
      ?: throw ReviewScheduleNotFoundException(prisonNumber)

  /**
   * Updates the prisoner's active Review Schedule by setting its status to EXEMPT_PRISONER_TRANSFER, then immediately
   * re-scheduling it.
   * Applying both status changes in quick succession (EXEMPT_PRISONER_TRANSFER, immediately followed by SCHEDULED) allows
   * for the full history of state changes of the [ReviewSchedule] to be maintained.
   *
   * The prisoner's active Review Schedule is the one with the status SCHEDULED or one of the EXEMPT_ statuses.
   * A Review Schedule with the status COMPLETE is not considered 'active'
   */
  fun exemptAndReScheduleActiveReviewScheduleStatusDueToPrisonerTransfer(prisonNumber: String, prisonTransferredTo: String) =
    reviewSchedulePersistenceAdapter.getActiveReviewSchedule(prisonNumber)
      ?.run {
        updateReviewScheduleFollowingPrisonerTransfer(this, prisonTransferredTo)
          .also {
            log.debug { "Review Schedule for prisoner [$prisonNumber] set to exempt: EXEMPT_PRISONER_TRANSFER, and then re-scheduled" }
          }
      }
      ?: throw ReviewScheduleNotFoundException(prisonNumber)

  private fun updateExemptStatus(
    reviewSchedule: ReviewSchedule,
    newStatus: ReviewScheduleStatus,
    exemptionReason: String?,
    prisonId: String,
    prisonNumber: String,
  ) {
    val updatedReviewSchedule = reviewSchedulePersistenceAdapter.updateReviewScheduleStatus(
      UpdateReviewScheduleStatusDto(
        reference = reviewSchedule.reference,
        scheduleStatus = newStatus,
        exemptionReason = exemptionReason,
        prisonId = prisonId,
        prisonNumber = prisonNumber,
      ),
    )
    performFollowOnEvents(
      updatedReviewSchedule = updatedReviewSchedule,
      oldStatus = reviewSchedule.scheduleStatus,
      oldReviewDate = reviewSchedule.reviewScheduleWindow.dateTo,
    )
  }

  private fun updateScheduledStatus(
    reviewSchedule: ReviewSchedule,
    prisonId: String,
    prisonNumber: String,
  ) {
    val adjustedReviewDate = reviewScheduleDateCalculationService.calculateAdjustedReviewDueDate(reviewSchedule)
    val updatedReviewSchedule = reviewSchedulePersistenceAdapter.updateReviewScheduleStatus(
      UpdateReviewScheduleStatusDto(
        reference = reviewSchedule.reference,
        scheduleStatus = ReviewScheduleStatus.SCHEDULED,
        prisonId = prisonId,
        latestReviewDate = adjustedReviewDate,
        prisonNumber = prisonNumber,
      ),
    )
    performFollowOnEvents(
      updatedReviewSchedule = updatedReviewSchedule,
      oldStatus = reviewSchedule.scheduleStatus,
      oldReviewDate = reviewSchedule.reviewScheduleWindow.dateTo,
    )
  }

  private fun updateReviewScheduleFollowingSystemTechnicalIssue(
    reviewSchedule: ReviewSchedule,
    exemptionReason: String?,
    prisonId: String,
    prisonNumber: String,
  ) {
    // Update the review schedule status to EXEMPT_SYSTEM_TECHNICAL_ISSUE
    val updatedReviewScheduleFirst = reviewSchedulePersistenceAdapter.updateReviewScheduleStatus(
      UpdateReviewScheduleStatusDto(
        reference = reviewSchedule.reference,
        scheduleStatus = ReviewScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE,
        exemptionReason = exemptionReason,
        prisonId = prisonId,
        prisonNumber = prisonNumber,
      ),
    )
    performFollowOnEvents(
      updatedReviewSchedule = updatedReviewScheduleFirst,
      oldStatus = reviewSchedule.scheduleStatus,
      oldReviewDate = reviewSchedule.reviewScheduleWindow.dateTo,
    )

    // Then update the review schedule to be SCHEDULED with an adjusted review date
    val adjustedReviewDate = reviewScheduleDateCalculationService.calculateAdjustedReviewDueDate(updatedReviewScheduleFirst)
    val updatedReviewScheduleSecond = reviewSchedulePersistenceAdapter.updateReviewScheduleStatus(
      UpdateReviewScheduleStatusDto(
        reference = reviewSchedule.reference,
        scheduleStatus = ReviewScheduleStatus.SCHEDULED,
        prisonId = prisonId,
        latestReviewDate = adjustedReviewDate,
        prisonNumber = prisonNumber,
      ),
    )
    performFollowOnEvents(
      updatedReviewSchedule = updatedReviewScheduleSecond,
      oldStatus = updatedReviewScheduleFirst.scheduleStatus,
      oldReviewDate = updatedReviewScheduleFirst.reviewScheduleWindow.dateTo,
    )
  }

  private fun updateReviewScheduleFollowingPrisonerTransfer(
    reviewSchedule: ReviewSchedule,
    prisonTransferredTo: String,
  ) {
    with(reviewSchedule) {
      // The prison that the prisoner transferred from is not in the event details. We need it when updating the Review Schedule
      // with the EXEMPT_PRISONER_TRANSFER status to maintain the audit history fields. The best we can do is to use the `lastUpdatedAtPrison`
      // field from the current ReviewSchedule record before any changes are made.
      val prisonTransferredFrom = lastUpdatedAtPrison
      // Update the review schedule status to EXEMPT_PRISONER_TRANSFER
      val updatedReviewScheduleFirst = reviewSchedulePersistenceAdapter.updateReviewScheduleStatus(
        UpdateReviewScheduleStatusDto(
          reference = reference,
          scheduleStatus = ReviewScheduleStatus.EXEMPT_PRISONER_TRANSFER,
          prisonId = prisonTransferredFrom,
          prisonNumber = prisonNumber,
        ),
      )
      performFollowOnEvents(
        updatedReviewSchedule = updatedReviewScheduleFirst,
        oldStatus = scheduleStatus,
        oldReviewDate = reviewScheduleWindow.dateTo,
      )

      // Then update the review schedule to be SCHEDULED with an adjusted review date
      val adjustedReviewDate = reviewScheduleDateCalculationService.calculateAdjustedReviewDueDate(updatedReviewScheduleFirst)
      val updatedReviewScheduleSecond = reviewSchedulePersistenceAdapter.updateReviewScheduleStatus(
        UpdateReviewScheduleStatusDto(
          reference = reference,
          scheduleStatus = ReviewScheduleStatus.SCHEDULED,
          prisonId = prisonTransferredTo,
          latestReviewDate = adjustedReviewDate,
          prisonNumber = prisonNumber,
        ),
      )
      performFollowOnEvents(
        updatedReviewSchedule = updatedReviewScheduleSecond,
        oldStatus = updatedReviewScheduleFirst.scheduleStatus,
        oldReviewDate = updatedReviewScheduleFirst.reviewScheduleWindow.dateTo,
      )
    }
  }

  private fun performFollowOnEvents(
    oldStatus: ReviewScheduleStatus,
    oldReviewDate: LocalDate,
    updatedReviewSchedule: ReviewSchedule,
  ) {
    reviewScheduleEventService.reviewScheduleStatusUpdated(
      UpdatedReviewScheduleStatus(
        reference = updatedReviewSchedule.reference,
        prisonNumber = updatedReviewSchedule.prisonNumber,
        updatedAtPrison = updatedReviewSchedule.lastUpdatedAtPrison,
        oldStatus = oldStatus,
        newStatus = updatedReviewSchedule.scheduleStatus,
        exemptionReason = updatedReviewSchedule.exemptionReason,
        oldReviewDate = oldReviewDate,
        newReviewDate = updatedReviewSchedule.reviewScheduleWindow.dateTo,
        updatedAt = updatedReviewSchedule.lastUpdatedAt,
        updatedBy = updatedReviewSchedule.lastUpdatedBy,
      ),
    )
  }
}
