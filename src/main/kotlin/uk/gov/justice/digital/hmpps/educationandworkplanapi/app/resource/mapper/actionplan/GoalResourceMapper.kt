package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CompleteGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UnarchiveGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UpdateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.note.NoteResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ArchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CompleteGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UnarchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateGoalRequest
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus as GoalStatusDomain

@Component
class GoalResourceMapper(
  private val stepResourceMapper: StepResourceMapper,
  private val noteResourceMapper: NoteResourceMapper,
  private val instantMapper: InstantMapper,
  private val userService: ManageUserService,
) {
  fun fromModelToDto(createGoalRequest: CreateGoalRequest): CreateGoalDto = with(createGoalRequest) {
    CreateGoalDto(
      title = title,
      steps = steps.map { stepResourceMapper.fromModelToDto(it) },
      prisonId = prisonId,
      targetCompletionDate = targetCompletionDate,
      notes = notes,
      status = GoalStatusDomain.ACTIVE,
    )
  }

  fun fromModelToDto(updateGoalRequest: UpdateGoalRequest): UpdateGoalDto = with(updateGoalRequest) {
    UpdateGoalDto(
      reference = goalReference,
      steps = steps.map { stepResourceMapper.fromModelToDto(it) },
      title = title,
      prisonId = prisonId,
      targetCompletionDate = targetCompletionDate,
      notes = notes,
    )
  }

  fun fromDomainToModel(goalDomain: Goal, goalDomainNotes: List<NoteDto>): GoalResponse = with(goalDomain) {
    GoalResponse(
      goalReference = reference,
      title = title,
      steps = steps.map { stepResourceMapper.fromDomainToModel(it) },
      targetCompletionDate = targetCompletionDate,
      status = toGoalStatus(status),
      archiveReason = archiveReason?.let { toReasonToArchiveGoal(it) },
      archiveReasonOther = archiveReasonOther,
      createdBy = createdBy!!,
      createdByDisplayName = userService.getUserDetails(createdBy!!).name,
      createdAt = instantMapper.toOffsetDateTime(createdAt)!!,
      createdAtPrison = createdAtPrison,
      updatedBy = lastUpdatedBy!!,
      updatedByDisplayName = userService.getUserDetails(lastUpdatedBy!!).name,
      updatedAt = instantMapper.toOffsetDateTime(lastUpdatedAt)!!,
      updatedAtPrison = lastUpdatedAtPrison,
      notes = notes,
      goalNotes = goalDomainNotes.map { noteResourceMapper.fromDomainToModel(it) },
    )
  }

  fun fromModelToDto(archiveGoalRequest: ArchiveGoalRequest): ArchiveGoalDto = with(archiveGoalRequest) {
    ArchiveGoalDto(
      reference = goalReference,
      reason = toReasonToArchiveGoal(reason),
      reasonOther = reasonOther,
      prisonId = prisonId,
    )
  }

  fun fromModelToDto(unarchiveGoalRequest: UnarchiveGoalRequest): UnarchiveGoalDto = UnarchiveGoalDto(reference = unarchiveGoalRequest.goalReference, prisonId = unarchiveGoalRequest.prisonId)

  fun fromModelToDto(completeGoalRequest: CompleteGoalRequest): CompleteGoalDto = CompleteGoalDto(reference = completeGoalRequest.goalReference, prisonId = completeGoalRequest.prisonId)
}
