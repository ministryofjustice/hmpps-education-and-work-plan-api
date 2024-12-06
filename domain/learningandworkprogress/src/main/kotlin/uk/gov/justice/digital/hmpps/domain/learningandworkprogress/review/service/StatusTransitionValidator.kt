package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.InvalidReviewScheduleStatusException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.COMPLETED
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.EXEMPT_PRISONER_DEATH
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.EXEMPT_PRISONER_RELEASE
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.EXEMPT_PRISONER_TRANSFER
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.SCHEDULED

class StatusTransitionValidator {

  fun validate(prisonNumber: String, currentStatus: String, newStatus: String) {
    when {
      isExemptOrCompletedTransitionInvalid(currentStatus, newStatus) ->
        throw InvalidReviewScheduleStatusException(prisonNumber, currentStatus, newStatus)

      isScheduledTransitionInvalid(currentStatus, newStatus) ->
        throw InvalidReviewScheduleStatusException(prisonNumber, currentStatus, newStatus)

      isRestrictedStatusTransition(currentStatus, newStatus) ->
        throw InvalidReviewScheduleStatusException(prisonNumber, currentStatus, newStatus)
    }
  }

  private fun isExemptOrCompletedTransitionInvalid(currentStatus: String, newStatus: String): Boolean =
    newStatus.startsWith("EXEMPT_") && (currentStatus.startsWith("EXEMPT_") || currentStatus == COMPLETED.name)

  private fun isScheduledTransitionInvalid(currentStatus: String, newStatus: String): Boolean =
    newStatus == "SCHEDULED" && !currentStatus.startsWith("EXEMPT_")

  private fun isRestrictedStatusTransition(currentStatus: String, newStatus: String): Boolean =
    currentStatus in restrictedStatuses && newStatus == SCHEDULED.name

  companion object {
    private val restrictedStatuses = setOf(
      EXEMPT_PRISONER_TRANSFER.name,
      EXEMPT_PRISONER_RELEASE.name,
      EXEMPT_PRISONER_DEATH.name,
    )
  }
}
