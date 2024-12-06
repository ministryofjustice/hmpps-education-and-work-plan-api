package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.UpdateReviewScheduleStatusDto
import java.time.LocalDate

private const val EXEMPTION_ADDITIONAL_DAYS = 5L
private const val EXCLUSION_ADDITIONAL_DAYS = 10L
private const val SYSTEM_OUTAGE_ADDITIONAL_DAYS = 5L

private val log = KotlinLogging.logger {}
class ReviewScheduleService(private val reviewSchedulePersistenceAdapter: ReviewSchedulePersistenceAdapter) {

  private val statusTransitionValidator = StatusTransitionValidator()

  fun updateLatestReviewScheduleStatus(
    prisonNumber: String,
    prisonId: String,
    newStatusString: String,
  ) {
    val newStatus = ReviewScheduleStatus.valueOf(newStatusString)
    val reviewSchedule = reviewSchedulePersistenceAdapter.getLatestReviewSchedule(prisonNumber)
      ?: throw ReviewScheduleNotFoundException(prisonNumber)

    // Validate the status transition
    statusTransitionValidator.validate(prisonNumber, reviewSchedule.scheduleStatus, newStatus)

    when {
      newStatus == ReviewScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE -> {
        handleSystemException(reviewSchedule, newStatus, prisonId, prisonNumber)
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
    reviewSchedulePersistenceAdapter.updateReviewScheduleStatus(
      UpdateReviewScheduleStatusDto(
        reviewSchedule.reference,
        newStatus,
        prisonId,
        prisonNumber = prisonNumber,
      ),
    )
    performFollowOnEvents(prisonNumber, newStatus, prisonId)
  }

  private fun updateScheduledStatus(
    reviewSchedule: ReviewSchedule,
    newStatus: ReviewScheduleStatus,
    prisonId: String,
    prisonNumber: String,
  ) {
    val newReviewDate = calculateNewReviewDate(reviewSchedule)
    reviewSchedulePersistenceAdapter.updateReviewScheduleStatus(
      UpdateReviewScheduleStatusDto(
        reviewSchedule.reference,
        ReviewScheduleStatus.SCHEDULED,
        prisonId,
        latestReviewDate = newReviewDate,
        prisonNumber = prisonNumber,
      ),
    )
    performFollowOnEvents(prisonNumber, newStatus, prisonId, newReviewDate)
  }

  private fun handleSystemException(
    reviewSchedule: ReviewSchedule,
    newStatus: ReviewScheduleStatus,
    prisonId: String,
    prisonNumber: String,
  ) {
    // Update the review schedule status to EXEMPT_SYSTEM_TECHNICAL_ISSUE
    reviewSchedulePersistenceAdapter.updateReviewScheduleStatus(
      UpdateReviewScheduleStatusDto(
        reviewSchedule.reference,
        newStatus,
        prisonId,
        prisonNumber = prisonNumber,
      ),
    )
    performFollowOnEvents(prisonNumber, newStatus, prisonId)

    // Then update the review schedule to be SCHEDULED with a new review date
    val newReviewDate = calculateNewReviewDate(reviewSchedule, SYSTEM_OUTAGE_ADDITIONAL_DAYS)
    reviewSchedulePersistenceAdapter.updateReviewScheduleStatus(
      UpdateReviewScheduleStatusDto(
        reviewSchedule.reference,
        ReviewScheduleStatus.SCHEDULED,
        prisonId,
        latestReviewDate = newReviewDate,
        prisonNumber = prisonNumber,
      ),
    )
    performFollowOnEvents(prisonNumber, ReviewScheduleStatus.SCHEDULED, prisonId, newReviewDate)
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
    prisonNumber: String,
    status: ReviewScheduleStatus,
    prisonId: String,
    newReviewDate: LocalDate? = null,
  ) {
    // TODO: Handle telemetry, timeline, and outbound message
  }
}
