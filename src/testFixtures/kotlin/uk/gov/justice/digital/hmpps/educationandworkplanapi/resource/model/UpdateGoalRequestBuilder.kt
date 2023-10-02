package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidReference
import java.time.LocalDate
import java.util.UUID

fun aValidUpdateGoalRequest(
  goalReference: UUID = aValidReference(),
  title: String = "Improve communication skills",
  targetCompletionDate: LocalDate = LocalDate.now().plusMonths(6),
  status: GoalStatus = GoalStatus.ACTIVE,
  steps: List<UpdateStepRequest> = listOf(aValidUpdateStepRequest()),
  notes: String? = "Chris would like to improve his listening skills, not just his verbal communication",
  prisonId: String = "BXI",
): UpdateGoalRequest =
  UpdateGoalRequest(
    goalReference = goalReference,
    title = title,
    targetCompletionDate = targetCompletionDate,
    status = status,
    steps = steps,
    notes = notes,
    prisonId = prisonId,
  )
