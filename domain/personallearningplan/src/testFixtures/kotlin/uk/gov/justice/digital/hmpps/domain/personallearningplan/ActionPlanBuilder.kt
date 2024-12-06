package uk.gov.justice.digital.hmpps.domain.personallearningplan

import java.util.UUID

fun aValidActionPlan(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234BC",
  goals: List<Goal> = mutableListOf(aValidGoal()),
): ActionPlan =
  ActionPlan(
    reference = reference,
    prisonNumber = prisonNumber,
    goals = goals,
  )
