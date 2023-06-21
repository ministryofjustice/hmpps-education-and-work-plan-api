package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

fun aValidActionPlanResponse(
  prisonNumber: String = "A1234BC",
  goals: List<GoalResponse> = listOf(aValidGoalResponse(), anotherValidGoalResponse()),
): ActionPlanResponse =
  ActionPlanResponse(
    prisonNumber = prisonNumber,
    goals = goals,
  )
