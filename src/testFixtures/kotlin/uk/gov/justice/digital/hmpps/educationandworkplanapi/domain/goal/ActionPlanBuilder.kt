package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal

import java.time.LocalDate
import java.util.UUID

fun aValidActionPlan(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234BC",
  reviewDateCategory: ReviewDateCategory = ReviewDateCategory.SPECIFIC_DATE,
  reviewDate: LocalDate? = LocalDate.now().plusMonths(6),
  goals: List<Goal> = mutableListOf(aValidGoal()),
): ActionPlan =
  ActionPlan(
    reference = reference,
    prisonNumber = prisonNumber,
    reviewDateCategory = reviewDateCategory,
    reviewDate = reviewDate,
    goals = goals,
  )
