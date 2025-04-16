package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UnarchiveGoalRequest
import java.util.*

fun aValidUnarchiveGoalRequest(
  goalReference: UUID = aValidReference(),
  prisonId: String = "BXI",
): UnarchiveGoalRequest = UnarchiveGoalRequest(
  goalReference = goalReference,
  prisonId = prisonId,
)
