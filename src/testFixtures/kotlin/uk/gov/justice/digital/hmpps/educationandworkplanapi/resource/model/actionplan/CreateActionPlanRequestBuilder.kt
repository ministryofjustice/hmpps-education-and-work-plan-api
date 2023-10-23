package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest
import java.time.LocalDate

fun aValidCreateActionPlanRequest(
  reviewDate: LocalDate? = LocalDate.now().plusMonths(6),
  goals: List<CreateGoalRequest> = listOf(aValidCreateGoalRequest()),
): CreateActionPlanRequest =
  CreateActionPlanRequest(
    reviewDate = reviewDate,
    goals = goals,
  )
