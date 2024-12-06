package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

/**
 * A DTO class that contains the data required to create a new Action Plan domain object
 */
data class CreateActionPlanDto(
  val prisonNumber: String,
  val goals: List<CreateGoalDto>,
)
