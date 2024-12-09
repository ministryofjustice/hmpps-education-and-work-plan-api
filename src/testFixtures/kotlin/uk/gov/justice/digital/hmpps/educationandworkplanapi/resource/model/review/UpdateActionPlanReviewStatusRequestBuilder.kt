package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateReviewScheduleStatusRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus

fun aValidUpdateActionPlanReviewStatusRequest(
  prisonId: String = "BXI",
  status: ReviewScheduleStatus = ReviewScheduleStatus.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY,
): CreateReviewScheduleStatusRequest =
  CreateReviewScheduleStatusRequest(
    prisonId = prisonId,
    status = status,
  )
