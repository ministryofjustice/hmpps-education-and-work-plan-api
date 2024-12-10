package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.UpdatedReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.UpdateReviewScheduleStatusDto
import java.time.LocalDate

private const val EXEMPTION_ADDITIONAL_DAYS = 5L
private const val EXCLUSION_ADDITIONAL_DAYS = 10L
private const val SYSTEM_OUTAGE_ADDITIONAL_DAYS = 5L

private val log = KotlinLogging.logger {}

class ReviewScheduleService(
  private val reviewSchedulePersistenceAdapter: ReviewSchedulePersistenceAdapter,
  private val reviewScheduleEventService: ReviewScheduleEventService,
) {

  private val reviewScheduleStatusTransitionValidator = ReviewScheduleStatusTransitionValidator()

  fun updateLatestReviewScheduleStatus(
    prisonNumber: String,
    prisonId: String,
    newStatus: ReviewScheduleStatus,
  ) {
    val reviewSchedule = reviewSchedulePersistenceAdapter.getLatestReviewSchedule(prisonNumber)
      ?: throw ReviewScheduleNotFoundException(prisonNumber)

    // Validate the status transition
    reviewScheduleStatusTransitionValidator.validate(prisonNumber, reviewSchedule.scheduleStatus, newStatus)

    when {
      newStatus == ReviewScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE -> {
        updateReviewScheduleFollowingSystemTechnicalIssue(reviewSchedule, prisonId, prisonNumber)
      }

      newStatus.isExemptionOrExclusion() -> {
        updateExemptStatus(reviewSchedule, newStatus, prisonId, prisonNumber)
      }

      else -> {
        updateScheduledStatus(reviewSchedule, newStatus, prisonId, prisonNumber)
      }
    }
  }

  private fun updateExemptStatus(
    reviewSchedule: ReviewSchedule,
    newStatus: ReviewScheduleStatus,
    prisonId: String,
    prisonNumber: String,
  ) {
    val updatedReviewSchedule = reviewSchedulePersistenceAdapter.updateReviewScheduleStatus(
      UpdateReviewScheduleStatusDto(
        reviewSchedule.reference,
        newStatus,
        prisonId,
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
    newStatus: ReviewScheduleStatus,
    prisonId: String,
    prisonNumber: String,
  ) {
    val newReviewDate = calculateNewReviewDate(reviewSchedule)
    val updatedReviewSchedule = reviewSchedulePersistenceAdapter.updateReviewScheduleStatus(
      UpdateReviewScheduleStatusDto(
        reviewSchedule.reference,
        ReviewScheduleStatus.SCHEDULED,
        prisonId,
        latestReviewDate = newReviewDate,
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
    prisonId: String,
    prisonNumber: String,
  ) {
    // Update the review schedule status to EXEMPT_SYSTEM_TECHNICAL_ISSUE
    val updatedReviewScheduleFirst = reviewSchedulePersistenceAdapter.updateReviewScheduleStatus(
      UpdateReviewScheduleStatusDto(
        reviewSchedule.reference,
        ReviewScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE,
        prisonId,
        prisonNumber = prisonNumber,
      ),
    )
    performFollowOnEvents(
      updatedReviewSchedule = updatedReviewScheduleFirst,
      oldStatus = reviewSchedule.scheduleStatus,
      oldReviewDate = reviewSchedule.reviewScheduleWindow.dateTo,
    )

    // Then update the review schedule to be SCHEDULED with a new review date
    val newReviewDate = calculateNewReviewDate(reviewSchedule, SYSTEM_OUTAGE_ADDITIONAL_DAYS)
    val updatedReviewScheduleSecond = reviewSchedulePersistenceAdapter.updateReviewScheduleStatus(
      UpdateReviewScheduleStatusDto(
        reviewSchedule.reference,
        ReviewScheduleStatus.SCHEDULED,
        prisonId,
        latestReviewDate = newReviewDate,
        prisonNumber = prisonNumber,
      ),
    )
    performFollowOnEvents(
      updatedReviewSchedule = updatedReviewScheduleSecond,
      oldStatus = updatedReviewScheduleFirst.scheduleStatus,
      oldReviewDate = updatedReviewScheduleFirst.reviewScheduleWindow.dateTo,
    )
  }

  private fun calculateNewReviewDate(
    reviewSchedule: ReviewSchedule,
    additionalDays: Long = getExtensionDays(reviewSchedule.scheduleStatus),
  ): LocalDate? {
    return reviewSchedule.reviewScheduleWindow.dateTo.takeIf { it <= LocalDate.now() }
      ?.let { LocalDate.now().plusDays(additionalDays) }
  }

  private fun getExtensionDays(status: ReviewScheduleStatus): Long {
    return when {
      status.isExclusion -> EXCLUSION_ADDITIONAL_DAYS
      status.isExemption -> EXEMPTION_ADDITIONAL_DAYS
      else -> 0 // Default case, if no condition matches
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
        oldReviewDate = oldReviewDate,
        newReviewDate = updatedReviewSchedule.reviewScheduleWindow.dateTo,
        updatedAt = updatedReviewSchedule.lastUpdatedAt,
        updatedBy = updatedReviewSchedule.lastUpdatedBy,
      ),
    )
  }
}
