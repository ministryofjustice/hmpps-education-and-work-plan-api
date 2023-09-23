package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

fun aValidCreateGoalsRequest(
  goals: List<CreateGoalRequest> = listOf(aValidCreateGoalRequest()),
): CreateGoalsRequest =
  CreateGoalsRequest(
    goals = goals,
  )

fun anotherValidCreateGoalsRequest(
  goals: List<CreateGoalRequest> = listOf(anotherValidCreateGoalRequest()),
): CreateGoalsRequest =
  CreateGoalsRequest(
    goals = goals,
  )
