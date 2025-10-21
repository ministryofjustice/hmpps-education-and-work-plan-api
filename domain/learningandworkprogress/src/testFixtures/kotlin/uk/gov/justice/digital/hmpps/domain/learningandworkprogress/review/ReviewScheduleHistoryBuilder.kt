package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review

import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun aValidReviewScheduleHistory(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = randomValidPrisonNumber(),
  earliestReviewDate: LocalDate = LocalDate.now().minusMonths(1),
  latestReviewDate: LocalDate = LocalDate.now().plusMonths(1),
  scheduleCalculationRule: ReviewScheduleCalculationRule = ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
  scheduleStatus: ReviewScheduleStatus = ReviewScheduleStatus.SCHEDULED,
  exemptionReason: String? = null,
  createdBy: String = "asmith_gen",
  createdAt: Instant = Instant.now(),
  createdAtPrison: String = "BXI",
  lastUpdatedBy: String = "bjones_gen",
  lastUpdatedAt: Instant = Instant.now(),
  lastUpdatedAtPrison: String = "BXI",
  version: Int = 1,
): ReviewScheduleHistory = ReviewScheduleHistory(
  reference = reference,
  prisonNumber = prisonNumber,
  earliestReviewDate = earliestReviewDate,
  latestReviewDate = latestReviewDate,
  scheduleCalculationRule = scheduleCalculationRule,
  scheduleStatus = scheduleStatus,
  exemptionReason = exemptionReason,
  createdBy = createdBy,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  lastUpdatedBy = lastUpdatedBy,
  lastUpdatedAt = lastUpdatedAt,
  lastUpdatedAtPrison = lastUpdatedAtPrison,
  version = version,
)
