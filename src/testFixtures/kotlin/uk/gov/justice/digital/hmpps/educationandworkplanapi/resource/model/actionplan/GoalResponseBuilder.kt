package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonToArchiveGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepResponse
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

fun aValidGoalResponse(
  reference: UUID = UUID.randomUUID(),
  title: String = "Improve communication skills",
  targetCompletionDate: LocalDate = LocalDate.now().plusMonths(6),
  steps: List<StepResponse> = listOf(aValidStepResponse(), anotherValidStepResponse()),
  status: GoalStatus = GoalStatus.ACTIVE,
  notes: String? = "Chris would like to improve his listening skills, not just his verbal communication",
  createdBy: String = "asmith_gen",
  createdByDisplayName: String = "Alex Smith",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "asmith_gen",
  updatedByDisplayName: String = "Alex Smith",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedAtPrison: String = "BXI",
  archiveReason: ReasonToArchiveGoal? = null,
  archiveReasonOther: String? = null,
): GoalResponse =
  GoalResponse(
    goalReference = reference,
    title = title,
    targetCompletionDate = targetCompletionDate,
    steps = steps,
    status = status,
    notes = notes,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    updatedBy = updatedBy,
    updatedByDisplayName = updatedByDisplayName,
    updatedAt = updatedAt,
    updatedAtPrison = updatedAtPrison,
    archiveReason = archiveReason,
    archiveReasonOther = archiveReasonOther,
  )

fun anotherValidGoalResponse(
  reference: UUID = UUID.randomUUID(),
  title: String = "Learn bricklaying",
  targetCompletionDate: LocalDate = LocalDate.now().plusMonths(6),
  steps: List<StepResponse> = listOf(aValidStepResponse(title = "Attend in house bricklaying course")),
  status: GoalStatus = GoalStatus.ACTIVE,
  notes: String? = null,
  createdBy: String = "bjones_gen",
  createdByDisplayName: String = "Barry Jones",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "bjones_gen",
  updatedByDisplayName: String = "Barry Jones",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedAtPrison: String = "BXI",
): GoalResponse =
  GoalResponse(
    goalReference = reference,
    title = title,
    targetCompletionDate = targetCompletionDate,
    steps = steps,
    status = status,
    notes = notes,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    updatedBy = updatedBy,
    updatedAt = updatedAt,
    updatedByDisplayName = updatedByDisplayName,
    updatedAtPrison = updatedAtPrison,
  )
