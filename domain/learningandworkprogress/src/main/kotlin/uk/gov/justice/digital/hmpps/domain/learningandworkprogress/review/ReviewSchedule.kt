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
  val earliestReviewDate: LocalDate,
  val latestReviewDate: LocalDate,
  val scheduleCalculationRule: ReviewScheduleCalculationRule,
  val scheduleStatus: ReviewScheduleStatus,
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
  LESS_THAN_6_MONTHS_TO_SERVE,
  BETWEEN_6_AND_12_MONTHS_TO_SERVE,
  BETWEEN_12_AND_60_MONTHS_TO_SERVE,
  INDETERMINATE_SENTENCE,
  PRISONER_ON_REMAND,
  PRISONER_UN_SENTENCED,
}

enum class ReviewScheduleStatus(val inScope: Boolean) {
  SCHEDULED(true),
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
}
