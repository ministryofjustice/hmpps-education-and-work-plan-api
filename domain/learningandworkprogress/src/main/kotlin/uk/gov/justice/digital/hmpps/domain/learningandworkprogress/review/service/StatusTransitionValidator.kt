package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.InvalidReviewScheduleStatusException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.COMPLETED
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.EXEMPT_PRISONER_DEATH
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.EXEMPT_PRISONER_RELEASE
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.EXEMPT_PRISONER_TRANSFER
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.SCHEDULED

class StatusTransitionValidator {

  fun validate(prisonNumber: String, currentStatus: ReviewScheduleStatus, newStatus: ReviewScheduleStatus) {
    when {
      isExemptOrCompletedTransitionInvalid(currentStatus, newStatus) ->
        throw InvalidReviewScheduleStatusException(prisonNumber, currentStatus.name, newStatus.name)

      isScheduledTransitionInvalid(currentStatus, newStatus) ->
        throw InvalidReviewScheduleStatusException(prisonNumber, currentStatus.name, newStatus.name)

      isRestrictedStatusTransition(currentStatus, newStatus) ->
        throw InvalidReviewScheduleStatusException(prisonNumber, currentStatus.name, newStatus.name)
    }
  }

  private fun isExemptOrCompletedTransitionInvalid(currentStatus: ReviewScheduleStatus, newStatus: ReviewScheduleStatus): Boolean =
    newStatus.isExemptionOrExclusion() && (currentStatus.isExemptionOrExclusion() || currentStatus == COMPLETED)

  private fun isScheduledTransitionInvalid(currentStatus: ReviewScheduleStatus, newStatus: ReviewScheduleStatus): Boolean =
    newStatus == SCHEDULED && !currentStatus.isExemptionOrExclusion()

  private fun isRestrictedStatusTransition(currentStatus: ReviewScheduleStatus, newStatus: ReviewScheduleStatus): Boolean =
    currentStatus in restrictedStatuses && newStatus == SCHEDULED

  companion object {
    private val restrictedStatuses = setOf(
      EXEMPT_PRISONER_TRANSFER,
      EXEMPT_PRISONER_RELEASE,
      EXEMPT_PRISONER_DEATH,
    )
  }
}
