package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * A prisoner's updated Review schedule.
 */
data class UpdatedReviewScheduleStatus(
  val reference: UUID,
  val prisonNumber: String,
  val updatedBy: String,
  val updatedAt: Instant,
  val updatedAtPrison: String,
  val oldStatus: ReviewScheduleStatus,
  val newStatus: ReviewScheduleStatus,
  val oldReviewDate: LocalDate,
  val newReviewDate: LocalDate,

)
