package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalStatus.ACTIVE
import java.time.Instant
import java.time.LocalDate
import java.util.*

fun aValidGoalEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  title: String = "Improve communication skills",
  targetCompletionDate: LocalDate = LocalDate.now().plusMonths(6),
  status: GoalStatus = ACTIVE,
  steps: List<StepEntity> = listOf(aValidStepEntity()),
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String? = "asmith_gen",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
  archiveReason: ReasonToArchiveGoal? = null,
  archiveReasonOther: String? = null,
): GoalEntity = GoalEntity(
  reference = reference,
  title = title,
  targetCompletionDate = targetCompletionDate,
  status = status,
  steps = steps.toMutableList(),
  createdAtPrison = createdAtPrison,
  updatedAtPrison = updatedAtPrison,
  archiveReason = archiveReason,
  archiveReasonOther = archiveReasonOther,
).apply {
  this.id = id
  this.createdAt = createdAt
  this.createdBy = createdBy
  this.updatedAt = updatedAt
  this.updatedBy = updatedBy
}
