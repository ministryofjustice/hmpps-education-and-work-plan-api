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
}

enum class ReviewScheduleStatus(val inScope: Boolean, val isExclusion: Boolean = false, val isExemption: Boolean = false) {
  SCHEDULED(true),
  EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY(false, isExclusion = true),
  EXEMPT_PRISONER_OTHER_HEALTH_ISSUES(false, isExclusion = true),
  EXEMPT_PRISONER_FAILED_TO_ENGAGE(false, isExemption = true),
  EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED(false, isExemption = true),
  EXEMPT_PRISONER_SAFETY_ISSUES(false, isExclusion = true),
  EXEMPT_PRISON_REGIME_CIRCUMSTANCES(false, isExclusion = true),
  EXEMPT_PRISON_STAFF_REDEPLOYMENT(false, isExemption = true),
  EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE(false, isExemption = true),
  EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF(false, isExclusion = true),
  EXEMPT_SYSTEM_TECHNICAL_ISSUE(false), // system down
  EXEMPT_PRISONER_TRANSFER(false, isExemption = true),
  EXEMPT_PRISONER_RELEASE(false, isExemption = true),
  EXEMPT_PRISONER_DEATH(false, isExemption = true),
  COMPLETED(false),
  ;

  fun isExemptionOrExclusion(): Boolean {
    return isExemption || isExclusion
  }
}

data class ReviewScheduleWindow(
  val dateFrom: LocalDate,
  val dateTo: LocalDate,
) {
  companion object {
    fun fromTodayToTenDays(): ReviewScheduleWindow = with(LocalDate.now()) {
      ReviewScheduleWindow(this, plusDays(10))
    }
    fun fromOneToThreeMonths(): ReviewScheduleWindow = with(LocalDate.now()) {
      ReviewScheduleWindow(plusMonths(1), plusMonths(3))
    }
    fun fromOneToThreeMonthsMinusDays(daysToSubtract: Int): ReviewScheduleWindow = with(LocalDate.now()) {
      ReviewScheduleWindow(plusMonths(1), plusMonths(3).minusDays(daysToSubtract.toLong()))
    }
    fun fromTwoToThreeMonths(): ReviewScheduleWindow = with(LocalDate.now()) {
      ReviewScheduleWindow(plusMonths(2), plusMonths(3))
    }
    fun fromFourToSixMonths(): ReviewScheduleWindow = with(LocalDate.now()) {
      ReviewScheduleWindow(plusMonths(4), plusMonths(6))
    }
    fun fromTenToTwelveMonths(): ReviewScheduleWindow = with(LocalDate.now()) {
      ReviewScheduleWindow(plusMonths(10), plusMonths(12))
    }
  }
}
