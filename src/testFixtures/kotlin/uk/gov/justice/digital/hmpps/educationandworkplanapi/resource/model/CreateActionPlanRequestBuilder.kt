package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import java.time.LocalDate

fun aValidCreateActionPlanRequest(
  reviewDate: LocalDate? = LocalDate.now().plusMonths(6),
  goals: List<CreateGoalRequest> = listOf(aValidCreateGoalRequest()),
): CreateActionPlanRequest =
  CreateActionPlanRequest(
    reviewDate = reviewDate,
    goals = goals,
  )
