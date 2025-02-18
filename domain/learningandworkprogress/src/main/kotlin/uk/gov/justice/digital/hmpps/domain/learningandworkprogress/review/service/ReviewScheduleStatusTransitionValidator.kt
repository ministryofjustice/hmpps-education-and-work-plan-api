package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.InvalidReviewScheduleStatusException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.COMPLETED
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.EXEMPT_PRISONER_DEATH
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.EXEMPT_PRISONER_MERGE
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.EXEMPT_PRISONER_RELEASE
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.EXEMPT_PRISONER_TRANSFER
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.EXEMPT_UNKNOWN
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.SCHEDULED

private val log = KotlinLogging.logger {}
class ReviewScheduleStatusTransitionValidator {

  fun validate(prisonNumber: String, currentStatus: ReviewScheduleStatus, newStatus: ReviewScheduleStatus) {
    validationRules
      .firstOrNull { it.condition(currentStatus, newStatus) }
      ?.let { rule ->
        throw InvalidReviewScheduleStatusException(
          prisonNumber,
          currentStatus,
          newStatus,
        ).also {
          logInvalidTransition(prisonNumber, currentStatus, newStatus, rule.reason)
        }
      }
  }

  private fun logInvalidTransition(
    prisonNumber: String,
    currentStatus: ReviewScheduleStatus,
    newStatus: ReviewScheduleStatus,
    reason: String,
  ) {
    log.info("Invalid transition: PrisonNumber=$prisonNumber, CurrentStatus=$currentStatus, NewStatus=$newStatus. Reason: $reason")
  }

  private data class ValidationRule(
    val condition: (ReviewScheduleStatus, ReviewScheduleStatus) -> Boolean,
    val reason: String,
  )

  private val validationRules = listOf(
    ValidationRule(
      condition = { current, _ -> current == COMPLETED },
      reason = "Cannot transition from COMPLETED to any other status.",
    ),
    ValidationRule(
      condition = { current, new -> current.isExemptionOrExclusion() && new.isExemptionOrExclusion() },
      reason = "Cannot transition from one exemption or exclusion status to another.",
    ),
    ValidationRule(
      condition = { current, new -> new == SCHEDULED && !current.isExemptionOrExclusion() },
      reason = "Can only transition to SCHEDULED if the current status is an exemption or exclusion.",
    ),
    ValidationRule(
      condition = { _, new -> new in unsupportedNewStatuses },
      reason = "Cannot transition to restricted statuses using this route.",
    ),
  )

  companion object {
    private val unsupportedNewStatuses = setOf(
      EXEMPT_PRISONER_TRANSFER,
      EXEMPT_PRISONER_RELEASE,
      EXEMPT_PRISONER_DEATH,
      EXEMPT_PRISONER_MERGE,
      EXEMPT_UNKNOWN,
      COMPLETED,
    )
  }
}
