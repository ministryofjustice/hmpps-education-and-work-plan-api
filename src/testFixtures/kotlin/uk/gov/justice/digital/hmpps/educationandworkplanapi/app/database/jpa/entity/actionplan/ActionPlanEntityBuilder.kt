package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan

import java.util.UUID

fun aValidActionPlanEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID? = UUID.randomUUID(),
  prisonNumber: String = "A1234BC",
  goals: List<GoalEntity> = listOf(aValidGoalEntity()),
): ActionPlanEntity =
  ActionPlanEntity(
    id = id,
    reference = reference,
    prisonNumber = prisonNumber,
    goals = goals.toMutableList(),
  )
