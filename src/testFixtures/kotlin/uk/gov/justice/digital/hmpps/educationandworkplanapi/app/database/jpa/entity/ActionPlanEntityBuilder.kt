package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity

import java.time.LocalDate
import java.util.UUID

fun aValidActionPlanEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID? = UUID.randomUUID(),
  prisonNumber: String = "A1234BC",
  reviewDate: LocalDate? = LocalDate.now().plusMonths(6),
  goals: List<GoalEntity> = listOf(aValidGoalEntity()),
): ActionPlanEntity =
  ActionPlanEntity(
    id = id,
    reference = reference,
    prisonNumber = prisonNumber,
    reviewDate = reviewDate,
    goals = goals.toMutableList(),
  )
