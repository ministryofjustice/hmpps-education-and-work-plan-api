package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal

fun aValidActionPlan(
  prisonNumber: String = "A1234BC",
  goals: List<Goal> = mutableListOf(aValidGoal()),
): ActionPlan =
  ActionPlan(
    prisonNumber = prisonNumber,
    goals = goals,
  )
