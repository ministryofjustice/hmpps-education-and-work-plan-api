package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalsRequest

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
