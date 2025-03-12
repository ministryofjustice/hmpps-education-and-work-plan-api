package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review

import uk.gov.justice.digital.hmpps.educationandworkplanapi.randomValidPrisonNumber
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun aValidReviewScheduleEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = randomValidPrisonNumber(),
  earliestReviewDate: LocalDate = LocalDate.now().minusMonths(1),
  latestReviewDate: LocalDate = LocalDate.now().plusMonths(1),
  scheduleCalculationRule: ReviewScheduleCalculationRule = ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
  scheduleStatus: ReviewScheduleStatus = ReviewScheduleStatus.SCHEDULED,
  exemptionReason: String? = null,
  createdBy: String? = "asmith_gen",
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  updatedAt: Instant? = Instant.now(),
  updatedBy: String? = "bjones_gen",
  updatedAtPrison: String = "BXI",
): ReviewScheduleEntity = ReviewScheduleEntity(
  reference = reference,
  prisonNumber = prisonNumber,
  earliestReviewDate = earliestReviewDate,
  latestReviewDate = latestReviewDate,
  scheduleCalculationRule = scheduleCalculationRule,
  scheduleStatus = scheduleStatus,
  exemptionReason = exemptionReason,
  createdAtPrison = createdAtPrison,
  updatedAtPrison = updatedAtPrison,
).apply {
  this.id = id
  this.createdBy = createdBy
  this.createdAt = createdAt
  this.updatedBy = updatedBy
  this.updatedAt = updatedAt
}

fun aValidUnPersistedReviewScheduleEntity(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = randomValidPrisonNumber(),
  earliestReviewDate: LocalDate = LocalDate.now().minusMonths(1),
  latestReviewDate: LocalDate = LocalDate.now().plusMonths(1),
  scheduleCalculationRule: ReviewScheduleCalculationRule = ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
  scheduleStatus: ReviewScheduleStatus = ReviewScheduleStatus.SCHEDULED,
  exemptionReason: String? = null,
  createdAtPrison: String = "BXI",
  updatedAtPrison: String = "BXI",
): ReviewScheduleEntity = aValidReviewScheduleEntity(
  id = null,
  reference = reference,
  prisonNumber = prisonNumber,
  earliestReviewDate = earliestReviewDate,
  latestReviewDate = latestReviewDate,
  scheduleCalculationRule = scheduleCalculationRule,
  scheduleStatus = scheduleStatus,
  exemptionReason = exemptionReason,
  createdBy = null,
  createdAt = null,
  createdAtPrison = createdAtPrison,
  updatedBy = null,
  updatedAt = null,
  updatedAtPrison = updatedAtPrison,
)
