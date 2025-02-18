package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ScheduledActionPlanReviewResponse
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

fun aValidScheduledActionPlanReviewResponse(
  reference: UUID = UUID.randomUUID(),
  reviewDateFrom: LocalDate = LocalDate.now().minusMonths(1),
  reviewDateTo: LocalDate = LocalDate.now().plusMonths(1),
  calculationRule: ReviewScheduleCalculationRule = ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
  status: ReviewScheduleStatus = ReviewScheduleStatus.SCHEDULED,
  createdBy: String = "asmith_gen",
  createdByDisplayName: String = "Alex Smith",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "bjones_gen",
  updatedByDisplayName: String = "Barry Jones",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedAtPrison: String = "BXI",
): ScheduledActionPlanReviewResponse = ScheduledActionPlanReviewResponse(
  reference = reference,
  reviewDateFrom = reviewDateFrom,
  reviewDateTo = reviewDateTo,
  status = status,
  calculationRule = calculationRule,
  createdBy = createdBy,
  createdByDisplayName = createdByDisplayName,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  updatedBy = updatedBy,
  updatedByDisplayName = updatedByDisplayName,
  updatedAt = updatedAt,
  updatedAtPrison = updatedAtPrison,
)
