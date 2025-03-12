package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction

import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun aValidInductionScheduleHistory(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = randomValidPrisonNumber(),
  deadlineDate: LocalDate = LocalDate.now().plusDays(30),
  scheduleCalculationRule: InductionScheduleCalculationRule = InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
  scheduleStatus: InductionScheduleStatus = InductionScheduleStatus.SCHEDULED,
  exemptionReason: String? = null,
  createdBy: String = "asmith_gen",
  createdAt: Instant = Instant.now(),
  lastUpdatedBy: String = "bjones_gen",
  lastUpdatedAt: Instant = Instant.now(),
  version: Int = 1,
  createdAtPrison: String = "BXI",
  updatedAtPrison: String = "BXI",
): InductionScheduleHistory =
  InductionScheduleHistory(
    reference = reference,
    prisonNumber = prisonNumber,
    deadlineDate = deadlineDate,
    scheduleCalculationRule = scheduleCalculationRule,
    scheduleStatus = scheduleStatus,
    createdBy = createdBy,
    createdAt = createdAt,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedAt = lastUpdatedAt,
    exemptionReason = exemptionReason,
    version = version,
    createdAtPrison = createdAtPrison,
    lastUpdatedAtPrison = updatedAtPrison,
  )
