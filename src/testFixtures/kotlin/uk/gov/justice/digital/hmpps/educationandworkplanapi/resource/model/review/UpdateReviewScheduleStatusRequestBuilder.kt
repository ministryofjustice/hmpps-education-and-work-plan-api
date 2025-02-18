package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateReviewScheduleStatusRequest

fun aValidUpdateReviewScheduleStatusRequest(
  prisonId: String = "BXI",
  status: ReviewScheduleStatus = ReviewScheduleStatus.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY,
  exemptionReason: String? = null,
): UpdateReviewScheduleStatusRequest = UpdateReviewScheduleStatusRequest(
  prisonId = prisonId,
  status = status,
  exemptionReason = exemptionReason,
)
