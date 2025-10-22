package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ScheduledActionPlanReviewResponse
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

fun aValidReviewScheduleResponse(
  reference: UUID = UUID.randomUUID(),
  createdBy: String = "asmith_gen",
  createdByDisplayName: String = "Alex Smith",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "asmith_gen",
  updatedByDisplayName: String = "Alex Smith",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedAtPrison: String = "BXI",
  reviewDateFrom: LocalDate = LocalDate.now().plusMonths(1),
  reviewDateTo: LocalDate = LocalDate.now().plusMonths(2),
): ScheduledActionPlanReviewResponse = ScheduledActionPlanReviewResponse(
  reference = reference,
  createdBy = createdBy,
  createdByDisplayName = createdByDisplayName,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  updatedBy = updatedBy,
  updatedByDisplayName = updatedByDisplayName,
  updatedAt = updatedAt,
  updatedAtPrison = updatedAtPrison,
  calculationRule = ReviewScheduleCalculationRule.PRISONER_READMISSION,
  status = ReviewScheduleStatus.COMPLETED,
  reviewDateFrom = reviewDateFrom,
  reviewDateTo = reviewDateTo,
)
