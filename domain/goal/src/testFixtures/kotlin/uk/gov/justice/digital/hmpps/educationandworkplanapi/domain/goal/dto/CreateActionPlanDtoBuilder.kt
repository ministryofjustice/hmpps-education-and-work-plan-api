package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto

import java.time.LocalDate

fun aValidCreateActionPlanDto(
  prisonNumber: String = "A1234BC",
  reviewDate: LocalDate? = LocalDate.now().plusMonths(6),
  goals: List<CreateGoalDto> = mutableListOf(aValidCreateGoalDto()),
): CreateActionPlanDto =
  CreateActionPlanDto(
    prisonNumber = prisonNumber,
    reviewDate = reviewDate,
    goals = goals,
  )
