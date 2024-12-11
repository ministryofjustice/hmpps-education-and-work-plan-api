package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalResponse
import java.util.UUID

fun aValidActionPlanResponse(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234BC",
  goals: List<GoalResponse> = listOf(aValidGoalResponse(), anotherValidGoalResponse()),
): ActionPlanResponse =
  ActionPlanResponse(
    reference = reference,
    prisonNumber = prisonNumber,
    goals = goals,
  )
