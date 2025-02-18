package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * A prisoner's updated Induction schedule.
 */
data class UpdatedInductionScheduleStatus(
  val reference: UUID,
  val prisonNumber: String,
  val updatedBy: String,
  val updatedAt: Instant,
  val updatedAtPrison: String,
  val oldStatus: InductionScheduleStatus,
  val newStatus: InductionScheduleStatus,
  val exemptionReason: String?,
  val oldDeadlineDate: LocalDate,
  val newDeadlineDate: LocalDate,

)
