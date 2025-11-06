package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * The schedule/deadline for a prisoner's Plan to be reviewed by.
 */
data class ReviewSchedule(
  val reference: UUID,
  val prisonNumber: String,
  val reviewScheduleWindow: ReviewScheduleWindow,
  val scheduleCalculationRule: ReviewScheduleCalculationRule,
  val scheduleStatus: ReviewScheduleStatus,
  val exemptionReason: String?,
  val createdBy: String,
  val createdAt: Instant,
  val createdAtPrison: String,
  val lastUpdatedBy: String,
  val lastUpdatedAt: Instant,
  val lastUpdatedAtPrison: String,
)

enum class ReviewScheduleCalculationRule {
  PRISONER_READMISSION,
  PRISONER_TRANSFER,
  BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE,
  BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE,
  BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE,
  BETWEEN_6_AND_12_MONTHS_TO_SERVE,
  BETWEEN_12_AND_60_MONTHS_TO_SERVE,
  MORE_THAN_60_MONTHS_TO_SERVE,
  INDETERMINATE_SENTENCE,
  PRISONER_ON_REMAND,
  PRISONER_UN_SENTENCED,
  RELEASE_DATE_IN_PAST,
}

enum class ReviewScheduleStatus(
  val isExclusion: Boolean = false,
  val isExemption: Boolean = false,
  val includeExemptionOnSummary: Boolean = false,
) {
  SCHEDULED,
  EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY(isExclusion = true, includeExemptionOnSummary = true),
  EXEMPT_PRISONER_OTHER_HEALTH_ISSUES(isExclusion = true, includeExemptionOnSummary = true),
  EXEMPT_PRISONER_FAILED_TO_ENGAGE(isExemption = true, includeExemptionOnSummary = true),
  EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED(isExemption = true, includeExemptionOnSummary = true),
  EXEMPT_PRISONER_SAFETY_ISSUES(isExclusion = true, includeExemptionOnSummary = true),
  EXEMPT_PRISON_REGIME_CIRCUMSTANCES(isExclusion = true, includeExemptionOnSummary = true),
  EXEMPT_PRISON_STAFF_REDEPLOYMENT(isExemption = true, includeExemptionOnSummary = true),
  EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE(isExemption = true, includeExemptionOnSummary = true),
  EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF(isExclusion = true, includeExemptionOnSummary = true),
  EXEMPT_SYSTEM_TECHNICAL_ISSUE(includeExemptionOnSummary = true), // system down
  EXEMPT_PRISONER_TRANSFER(isExemption = true),
  EXEMPT_PRISONER_RELEASE(isExemption = true),
  EXEMPT_PRISONER_DEATH(isExemption = true),
  EXEMPT_PRISONER_MERGE(isExemption = true),
  EXEMPT_UNKNOWN(includeExemptionOnSummary = true),
  EXEMPT_TEMP_ABSENCE(isExemption = true),
  COMPLETED,
  ;

  fun isExemptionOrExclusion(): Boolean = isExemption || isExclusion
}

data class ReviewScheduleWindow(
  val dateFrom: LocalDate,
  val dateTo: LocalDate,
) {
  companion object {
    fun fromTodayToTenDays(baseDate: LocalDate): ReviewScheduleWindow = ReviewScheduleWindow(baseDate, baseDate.plusDays(10))

    fun fromOneToThreeMonths(baseDate: LocalDate): ReviewScheduleWindow = ReviewScheduleWindow(baseDate.plusMonths(1), baseDate.plusMonths(3))

    fun fromOneMonthToSpecificDate(baseDate: LocalDate, dateTo: LocalDate): ReviewScheduleWindow = ReviewScheduleWindow(baseDate.plusMonths(1), dateTo)

    fun fromTwoToThreeMonths(baseDate: LocalDate): ReviewScheduleWindow = ReviewScheduleWindow(baseDate.plusMonths(2), baseDate.plusMonths(3))

    fun fromFourToSixMonths(baseDate: LocalDate): ReviewScheduleWindow = ReviewScheduleWindow(baseDate.plusMonths(4), baseDate.plusMonths(6))

    fun fromTenToTwelveMonths(baseDate: LocalDate): ReviewScheduleWindow = ReviewScheduleWindow(baseDate.plusMonths(10), baseDate.plusMonths(12))
  }
}
