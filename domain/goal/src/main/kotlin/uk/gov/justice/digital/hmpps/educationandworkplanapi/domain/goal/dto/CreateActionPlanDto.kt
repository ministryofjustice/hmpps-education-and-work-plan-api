package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto

import java.time.LocalDate

data class CreateActionPlanDto(
  val prisonNumber: String,
  val reviewDate: LocalDate?,
  val goals: List<CreateGoalDto>,
)
