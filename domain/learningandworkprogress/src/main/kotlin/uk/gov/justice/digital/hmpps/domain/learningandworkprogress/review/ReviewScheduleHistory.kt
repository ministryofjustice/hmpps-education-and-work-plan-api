package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * A schedule/deadline history record  for a prisoner's Plan to be reviewed by.
 */
data class ReviewScheduleHistory(
  val reference: UUID,
  val prisonNumber: String,
  val earliestReviewDate: LocalDate,
  val latestReviewDate: LocalDate,
  val scheduleCalculationRule: ReviewScheduleCalculationRule,
  val scheduleStatus: ReviewScheduleStatus,
  val exemptionReason: String?,
  val createdBy: String,
  val createdAt: Instant,
  val createdAtPrison: String,
  val lastUpdatedBy: String,
  val lastUpdatedAt: Instant,
  val lastUpdatedAtPrison: String,
  val version: Int,
)
