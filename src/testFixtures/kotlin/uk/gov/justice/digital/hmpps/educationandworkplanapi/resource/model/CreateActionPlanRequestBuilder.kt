package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import java.time.LocalDate

fun aValidCreateActionPlanRequest(
  reviewDateCategory: ReviewDateCategory = ReviewDateCategory.SPECIFIC_DATE,
  reviewDate: LocalDate? = LocalDate.now().plusMonths(6),
  goals: List<CreateGoalRequest> = listOf(aValidCreateGoalRequest()),
): CreateActionPlanRequest =
  CreateActionPlanRequest(
    reviewDateCategory = reviewDateCategory,
    reviewDate = reviewDate,
    goals = goals,
  )
