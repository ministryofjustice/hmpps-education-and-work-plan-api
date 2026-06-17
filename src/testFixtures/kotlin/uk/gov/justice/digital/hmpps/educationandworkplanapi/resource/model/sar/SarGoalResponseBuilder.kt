package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.sar

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonToArchiveGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SarGoalResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidStepResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.anotherValidStepResponse
import java.time.LocalDate
import java.time.OffsetDateTime

fun aValidSarGoalResponse(
  title: String = "Improve communication skills",
  targetCompletionDate: LocalDate = LocalDate.now().plusMonths(6),
  steps: List<StepResponse> = listOf(aValidStepResponse(), anotherValidStepResponse()),
  status: GoalStatus = GoalStatus.ACTIVE,
  goalNote: String? = "Chris would like to improve his listening skills, not just his verbal communication",
  completionNote: String? = null,
  archiveReason: ReasonToArchiveGoal? = null,
  archiveReasonOther: String? = null,
  archiveNote: String? = null,
  createdBy: String = "asmith_gen",
  createdByDisplayName: String = "Alex Smith",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "asmith_gen",
  updatedByDisplayName: String = "Alex Smith",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedAtPrison: String = "BXI",
): SarGoalResponse = SarGoalResponse(
  title = title,
  targetCompletionDate = targetCompletionDate,
  steps = steps,
  status = status,
  goalNote = goalNote,
  goalCompletionNote = completionNote,
  goalArchiveReason = archiveReason,
  goalArchiveReasonOther = archiveReasonOther,
  goalArchiveNote = archiveNote,
  createdBy = createdBy,
  createdByDisplayName = createdByDisplayName,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  updatedBy = updatedBy,
  updatedByDisplayName = updatedByDisplayName,
  updatedAt = updatedAt,
  updatedAtPrison = updatedAtPrison,
)
