package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction

import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun aValidInductionSchedule(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = aValidPrisonNumber(),
  deadlineDate: LocalDate = LocalDate.now().plusDays(30),
  scheduleCalculationRule: InductionScheduleCalculationRule = InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
  scheduleStatus: InductionScheduleStatus = InductionScheduleStatus.SCHEDULED,
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  createdAt: Instant? = Instant.now(),
  lastUpdatedBy: String? = "bjones_gen",
  lastUpdatedByDisplayName: String? = "Barry Jones",
  lastUpdatedAt: Instant? = Instant.now(),
  exemptionReason: String? = null,
): InductionSchedule =
  InductionSchedule(
    reference = reference,
    prisonNumber = prisonNumber,
    deadlineDate = deadlineDate,
    scheduleCalculationRule = scheduleCalculationRule,
    scheduleStatus = scheduleStatus,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedByDisplayName = lastUpdatedByDisplayName,
    lastUpdatedAt = lastUpdatedAt,
    exemptionReason = exemptionReason,
  )
