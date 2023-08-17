package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import java.time.LocalDate
import java.util.UUID

fun aValidActionPlanResponse(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234BC",
  reviewDateCategory: ReviewDateCategory = ReviewDateCategory.SPECIFIC_DATE,
  reviewDate: LocalDate? = LocalDate.now().plusMonths(6),
  goals: List<GoalResponse> = listOf(aValidGoalResponse(), anotherValidGoalResponse()),
): ActionPlanResponse =
  ActionPlanResponse(
    reference = reference,
    prisonNumber = prisonNumber,
    reviewDateCategory = reviewDateCategory,
    reviewDate = reviewDate,
    goals = goals,
  )
