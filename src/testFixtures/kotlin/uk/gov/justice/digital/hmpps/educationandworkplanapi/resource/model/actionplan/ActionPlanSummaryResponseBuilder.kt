package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanSummaryResponse
import java.util.UUID

fun aValidActionPlanSummaryResponse(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234BC",
): ActionPlanSummaryResponse = ActionPlanSummaryResponse(
  reference = reference,
  prisonNumber = prisonNumber,
)
