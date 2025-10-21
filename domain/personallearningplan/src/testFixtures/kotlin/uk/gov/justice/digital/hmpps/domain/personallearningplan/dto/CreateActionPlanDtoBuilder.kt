package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

fun aValidCreateActionPlanDto(
  prisonNumber: String = "A1234BC",
  goals: List<CreateGoalDto> = mutableListOf(aValidCreateGoalDto()),
): CreateActionPlanDto = CreateActionPlanDto(
  prisonNumber = prisonNumber,
  goals = goals,
)
