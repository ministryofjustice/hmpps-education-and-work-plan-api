package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest

fun aValidCreateActionPlanRequest(
  goals: List<CreateGoalRequest> = listOf(aValidCreateGoalRequest()),
): CreateActionPlanRequest =
  CreateActionPlanRequest(
    goals = goals,
  )
