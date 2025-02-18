package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun aPersistedInductionScheduleEntity(
  id: UUID = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = aValidPrisonNumber(),
  deadlineDate: LocalDate = LocalDate.now().plusDays(30),
  scheduleCalculationRule: InductionScheduleCalculationRule = InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
  scheduleStatus: InductionScheduleStatus = InductionScheduleStatus.SCHEDULED,
  createdBy: String = "auser_gen",
  createdAt: Instant = Instant.now(),
  updatedAt: Instant = Instant.now(),
  updatedBy: String = "auser_gen",
  createdAtPrison: String = "BXI",
  updatedAtPrison: String = "BXI",
): InductionScheduleEntity = InductionScheduleEntity(
  reference = reference,
  prisonNumber = prisonNumber,
  deadlineDate = deadlineDate,
  scheduleCalculationRule = scheduleCalculationRule,
  scheduleStatus = scheduleStatus,
  createdAtPrison = createdAtPrison,
  updatedAtPrison = updatedAtPrison,
).apply {
  this.id = id
  this.createdAt = createdAt
  this.createdBy = createdBy
  this.updatedAt = updatedAt
  this.updatedBy = updatedBy
}

fun anUnPersistedInductionScheduleEntity(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = aValidPrisonNumber(),
  deadlineDate: LocalDate = LocalDate.now().plusDays(30),
  scheduleCalculationRule: InductionScheduleCalculationRule = InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
  scheduleStatus: InductionScheduleStatus = InductionScheduleStatus.SCHEDULED,
  createdAtPrison: String = "BXI",
  updatedAtPrison: String = "BXI",
): InductionScheduleEntity = InductionScheduleEntity(
  reference = reference,
  prisonNumber = prisonNumber,
  deadlineDate = deadlineDate,
  scheduleCalculationRule = scheduleCalculationRule,
  scheduleStatus = scheduleStatus,
  updatedAtPrison = updatedAtPrison,
  createdAtPrison = createdAtPrison,
)
