package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InvalidInductionScheduleStatusException

private val log = KotlinLogging.logger {}
class InductionScheduleStatusTransitionValidator {

  fun validate(prisonNumber: String, currentStatus: InductionScheduleStatus, newStatus: InductionScheduleStatus) {
    validationRules
      .firstOrNull { it.condition(currentStatus, newStatus) }
      ?.let { rule ->
        throw InvalidInductionScheduleStatusException(
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
    currentStatus: InductionScheduleStatus,
    newStatus: InductionScheduleStatus,
    reason: String,
  ) {
    log.info("Invalid transition: PrisonNumber=$prisonNumber, CurrentStatus=$currentStatus, NewStatus=$newStatus. Reason: $reason")
  }

  private data class ValidationRule(
    val condition: (InductionScheduleStatus, InductionScheduleStatus) -> Boolean,
    val reason: String,
  )

  private val validationRules = listOf(
    ValidationRule(
      condition = { current, _ -> current == InductionScheduleStatus.COMPLETED },
      reason = "Cannot transition from COMPLETED to any other status.",
    ),
    ValidationRule(
      condition = { current, _ -> current == InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS },
      reason = "Cannot transition from PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS to any other status.",
    ),
    ValidationRule(
      condition = { current, new -> current.isExemptionOrExclusion() && new.isExemptionOrExclusion() },
      reason = "Cannot transition from one exemption or exclusion status to another.",
    ),
    ValidationRule(
      condition = { _, new -> new in unsupportedNewStatuses },
      reason = "Cannot transition to restricted statuses using this route.",
    ),
  )

  companion object {
    private val unsupportedNewStatuses = setOf(
      InductionScheduleStatus.EXEMPT_PRISONER_TRANSFER,
      InductionScheduleStatus.EXEMPT_PRISONER_RELEASE,
      InductionScheduleStatus.EXEMPT_PRISONER_DEATH,
      InductionScheduleStatus.EXEMPT_PRISONER_MERGE,
      InductionScheduleStatus.COMPLETED,
      InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS,
    )
  }
}
