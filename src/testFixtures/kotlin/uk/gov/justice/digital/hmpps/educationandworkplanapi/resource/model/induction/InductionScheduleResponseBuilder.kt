package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

fun aValidInductionScheduleResponse(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234AB",
  createdBy: String = "asmith_gen",
  createdByDisplayName: String = "Alex Smith",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "asmith_gen",
  updatedByDisplayName: String = "Alex Smith",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedAtPrison: String = "BXI",
  deadlineDate: LocalDate = LocalDate.now().plusYears(1),
): InductionScheduleResponse = InductionScheduleResponse(
  reference = reference,
  prisonNumber = prisonNumber,
  createdBy = createdBy,
  createdByDisplayName = createdByDisplayName,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  updatedBy = updatedBy,
  updatedByDisplayName = updatedByDisplayName,
  updatedAt = updatedAt,
  updatedAtPrison = updatedAtPrison,
  deadlineDate = deadlineDate,
  scheduleCalculationRule = InductionScheduleCalculationRule.EXISTING_PRISONER,
  scheduleStatus = InductionScheduleStatus.COMPLETED,
)
