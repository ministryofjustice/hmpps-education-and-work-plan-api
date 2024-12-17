package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * The schedule/deadline for a prisoner's Induction to be completed by.
 */
data class InductionSchedule(
  val reference: UUID,
  val prisonNumber: String,
  val deadlineDate: LocalDate,
  val scheduleCalculationRule: InductionScheduleCalculationRule,
  val scheduleStatus: InductionScheduleStatus,
  /**
   * The user ID of the person (logged-in user) who created the Induction.
   */
  val createdBy: String?,
  /**
   * The name of the logged-in user who created the Induction.
   */
  val createdByDisplayName: String?,
  /**
   * The timestamp when this Induction was created.
   */
  val createdAt: Instant?,
  /**
   * The user ID of the person (logged-in user) who updated the Induction.
   */
  val lastUpdatedBy: String?,
  /**
   * The name of the logged-in user who updated the Induction.
   */
  val lastUpdatedByDisplayName: String?,
  /**
   * The timestamp when this Induction was updated.
   */
  val lastUpdatedAt: Instant?,
)

enum class InductionScheduleCalculationRule(val existingPrisonerWhenScheduleCreated: Boolean) {
  NEW_PRISON_ADMISSION(false),
  EXISTING_PRISONER_LESS_THAN_6_MONTHS_TO_SERVE(true),
  EXISTING_PRISONER_BETWEEN_6_AND_12_MONTHS_TO_SERVE(true),
  EXISTING_PRISONER_BETWEEN_12_AND_60_MONTHS_TO_SERVE(true),
  EXISTING_PRISONER_INDETERMINATE_SENTENCE(true),
  EXISTING_PRISONER_ON_REMAND(true),
  EXISTING_PRISONER_UN_SENTENCED(true),
}

enum class InductionScheduleStatus(val inScope: Boolean) {
  SCHEDULED(true),
  COMPLETE(true),
  EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY(false),
  EXEMPT_PRISONER_OTHER_HEALTH_ISSUES(false),
  EXEMPT_PRISONER_FAILED_TO_ENGAGE(false),
  EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED(false),
  EXEMPT_PRISONER_SAFETY_ISSUES(false),
  EXEMPT_PRISON_REGIME_CIRCUMSTANCES(false),
  EXEMPT_PRISON_STAFF_REDEPLOYMENT(false),
  EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE(false),
  EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF(false),
  EXEMPT_SYSTEM_TECHNICAL_ISSUE(false),
  EXEMPT_PRISONER_TRANSFER(false),
  EXEMPT_PRISONER_RELEASE(false),
  EXEMPT_PRISONER_DEATH(false),
  EXEMPT_SCREENING_AND_ASSESSMENT_IN_PROGRESS(false),
  EXEMPT_SCREENING_AND_ASSESSMENT_INCOMPLETE(false),
}
