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
  val createdBy: String,
  /**
   * The timestamp when this Induction was created.
   */
  val createdAt: Instant,

  val createdAtPrison: String,
  /**
   * The user ID of the person (logged-in user) who updated the Induction.
   */
  val lastUpdatedBy: String,
  /**
   * The timestamp when this Induction was updated.
   */
  val lastUpdatedAt: Instant,

  val lastUpdatedAtPrison: String,

  var exemptionReason: String?,
)

enum class InductionScheduleCalculationRule(val existingPrisonerWhenScheduleCreated: Boolean) {
  NEW_PRISON_ADMISSION(false),
  NEW_PRISON_ADMISSION_EXTENDED_DEADLINE_PERIOD(false),
  EXISTING_PRISONER(true),
}

enum class InductionScheduleStatus(
  val isExclusion: Boolean = false,
  val isExemption: Boolean = false,
  val includeExemptionOnSummary: Boolean = false,
  val canBeSetByUserAction: Boolean = false,
) {
  PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS(includeExemptionOnSummary = true),
  SCHEDULED,
  EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY(isExclusion = true, includeExemptionOnSummary = true, canBeSetByUserAction = true),
  EXEMPT_PRISONER_OTHER_HEALTH_ISSUES(isExclusion = true, includeExemptionOnSummary = true, canBeSetByUserAction = true),
  EXEMPT_PRISONER_FAILED_TO_ENGAGE(isExemption = true, includeExemptionOnSummary = true, canBeSetByUserAction = true),
  EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED(isExemption = true, includeExemptionOnSummary = true, canBeSetByUserAction = true),
  EXEMPT_PRISONER_SAFETY_ISSUES(isExclusion = true, includeExemptionOnSummary = true, canBeSetByUserAction = true),
  EXEMPT_PRISON_REGIME_CIRCUMSTANCES(isExclusion = true, includeExemptionOnSummary = true, canBeSetByUserAction = true),
  EXEMPT_PRISON_STAFF_REDEPLOYMENT(isExemption = true, includeExemptionOnSummary = true, canBeSetByUserAction = true),
  EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE(isExemption = true, includeExemptionOnSummary = true, canBeSetByUserAction = true),
  EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF(isExclusion = true, includeExemptionOnSummary = true, canBeSetByUserAction = true),
  EXEMPT_SYSTEM_TECHNICAL_ISSUE(isExemption = true, includeExemptionOnSummary = true, canBeSetByUserAction = true), // system down
  EXEMPT_PRISONER_TRANSFER(isExemption = true),
  EXEMPT_TEMP_ABSENCE(isExemption = true),
  EXEMPT_PRISONER_RELEASE(isExemption = true),
  EXEMPT_PRISONER_RELEASE_HOSPITAL(isExemption = true),
  EXEMPT_PRISONER_DEATH(isExemption = true),
  EXEMPT_PRISONER_MERGE(isExemption = true),
  EXEMPT_SCREENING_AND_ASSESSMENT_IN_PROGRESS(isExemption = true, includeExemptionOnSummary = true, canBeSetByUserAction = true),
  EXEMPT_SCREENING_AND_ASSESSMENT_INCOMPLETE(isExemption = true, includeExemptionOnSummary = true, canBeSetByUserAction = true),
  COMPLETED,
  ;

  fun isExemptionOrExclusion(): Boolean = isExemption || isExclusion

  /**
   * True for exemptions applied automatically by the system in response to a prisoner lifecycle event - i.e.
   * EXEMPT_PRISONER_TRANSFER, EXEMPT_TEMP_ABSENCE, EXEMPT_PRISONER_RELEASE, EXEMPT_PRISONER_RELEASE_HOSPITAL,
   * EXEMPT_PRISONER_DEATH and EXEMPT_PRISONER_MERGE - as opposed to exemptions a user applies manually.
   * Used to decide whether, on re-admission under the PES contract, the Induction should be re-gated on the
   * prisoner's Screening & Assessments.
   */
  fun isAutomaticExemption(): Boolean = isExemptionOrExclusion() && !canBeSetByUserAction
}
