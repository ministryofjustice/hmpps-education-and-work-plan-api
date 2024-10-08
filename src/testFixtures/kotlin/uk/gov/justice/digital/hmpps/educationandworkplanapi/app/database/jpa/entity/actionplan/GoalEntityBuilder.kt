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
  createdByDisplayName: String? = "Alex Smith",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
  updatedByDisplayName: String? = "Barry Jones",
  archiveReason: ReasonToArchiveGoal? = null,
  archiveReasonOther: String? = null,
): GoalEntity =
  GoalEntity(
    id = id,
    reference = reference,
    title = title,
    targetCompletionDate = targetCompletionDate,
    status = status,
    steps = steps.toMutableList(),
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    updatedAt = updatedAt,
    updatedAtPrison = updatedAtPrison,
    updatedBy = updatedBy,
    updatedByDisplayName = updatedByDisplayName,
    archiveReason = archiveReason,
    archiveReasonOther = archiveReasonOther,
  )
