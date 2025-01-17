package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan

import java.time.Instant
import java.util.UUID

fun aValidActionPlanEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234BC",
  goals: List<GoalEntity> = listOf(aValidGoalEntity()),
  createdAt: Instant? = Instant.now(),
  createdBy: String? = "asmith_gen",
  updatedAt: Instant? = Instant.now(),
  updatedBy: String? = "bjones_gen",
): ActionPlanEntity =
  ActionPlanEntity(
    reference = reference,
    prisonNumber = prisonNumber,
    goals = goals.toMutableList(),
  ).apply {
    this.id = id
    this.createdAt = createdAt
    this.createdBy = createdBy
    this.updatedAt = updatedAt
    this.updatedBy = updatedBy
  }
