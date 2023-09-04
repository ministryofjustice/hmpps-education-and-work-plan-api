package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto

import java.time.LocalDate

/**
 * A DTO class that contains the data required to create a new Action Plan domain object
 */
data class CreateActionPlanDto(
  val prisonNumber: String,
  val reviewDate: LocalDate?,
  val goals: List<CreateGoalDto>,
)
