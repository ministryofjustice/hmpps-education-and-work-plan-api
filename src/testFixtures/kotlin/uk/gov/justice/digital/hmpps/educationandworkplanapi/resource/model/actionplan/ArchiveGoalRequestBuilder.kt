package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ArchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonToArchiveGoal
import java.util.*

fun aValidArchiveGoalRequest(
  goalReference: UUID = aValidReference(),
  reason: ReasonToArchiveGoal = ReasonToArchiveGoal.PRISONER_NO_LONGER_WANTS_TO_WORK_TOWARDS_GOAL,
  reasonOther: String? = null,
  note: String? = null,
  prisonId: String = "BXI",
): ArchiveGoalRequest = ArchiveGoalRequest(
  goalReference = goalReference,
  reason = reason,
  reasonOther = reasonOther,
  note = note,
  prisonId = prisonId,
)
