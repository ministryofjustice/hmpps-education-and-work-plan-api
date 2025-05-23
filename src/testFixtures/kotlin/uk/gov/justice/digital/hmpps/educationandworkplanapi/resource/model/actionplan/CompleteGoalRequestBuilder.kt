package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CompleteGoalRequest
import java.util.*

fun aValidCompleteGoalRequest(
  goalReference: UUID = aValidReference(),
  note: String? = null,
  prisonId: String = "BXI",
): CompleteGoalRequest = CompleteGoalRequest(
  goalReference = goalReference,
  note = note,
  prisonId = prisonId,
)
