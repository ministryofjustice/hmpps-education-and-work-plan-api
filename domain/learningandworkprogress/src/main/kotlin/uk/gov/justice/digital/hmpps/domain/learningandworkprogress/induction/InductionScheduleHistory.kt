package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * The schedule/deadline for a prisoner's Induction to be completed by.
 */
data class InductionScheduleHistory(
  val reference: UUID,
  val prisonNumber: String,
  val deadlineDate: LocalDate,
  val scheduleCalculationRule: InductionScheduleCalculationRule,
  val scheduleStatus: InductionScheduleStatus,
  var exemptionReason: String?,
  /**
   * The user ID of the person (logged-in user) who created the Induction.
   */
  val createdBy: String,

  val createdAtPrison: String,
  /**
   * The timestamp when this Induction was created.
   */
  val createdAt: Instant,
  /**
   * The user ID of the person (logged-in user) who updated the Induction.
   */
  val lastUpdatedBy: String,

  val lastUpdatedAtPrison: String,
  /**
   * The timestamp when this Induction was updated.
   */
  val lastUpdatedAt: Instant,
  val version: Int,
)
