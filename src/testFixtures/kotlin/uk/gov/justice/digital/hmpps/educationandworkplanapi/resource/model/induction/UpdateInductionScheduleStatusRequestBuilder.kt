package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateInductionScheduleStatusRequest

fun aValidUpdateInductionScheduleStatusRequest(
  prisonId: String = "BXI",
  status: InductionScheduleStatus = InductionScheduleStatus.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY,
  exemptionReason: String? = null,
): UpdateInductionScheduleStatusRequest = UpdateInductionScheduleStatusRequest(
  prisonId = prisonId,
  status = status,
  exemptionReason = exemptionReason,
)
