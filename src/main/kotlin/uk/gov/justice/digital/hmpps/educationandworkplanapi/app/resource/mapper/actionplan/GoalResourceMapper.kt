package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CompleteGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UnarchiveGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UpdateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ArchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CompleteGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UnarchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateGoalRequest
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus as GoalStatusDomain
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ReasonToArchiveGoal as ReasonToArchiveGoalDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalStatus as GoalStatusApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonToArchiveGoal as ReasonToArchiveGoalApi

@Component
class GoalResourceMapper(
  private val stepResourceMapper: StepResourceMapper,
  private val instantMapper: InstantMapper,
  private val userService: ManageUserService,
) {
  fun fromModelToDto(createGoalRequest: CreateGoalRequest): CreateGoalDto =
    with(createGoalRequest) {
      CreateGoalDto(
        title = title,
        steps = steps.map { stepResourceMapper.fromModelToDto(it) },
        prisonId = prisonId,
        targetCompletionDate = targetCompletionDate,
        notes = notes,
        status = GoalStatusDomain.ACTIVE,
      )
    }

  fun fromModelToDto(updateGoalRequest: UpdateGoalRequest): UpdateGoalDto =
    with(updateGoalRequest) {
      UpdateGoalDto(
        reference = goalReference,
        steps = steps.map { stepResourceMapper.fromModelToDto(it) },
        title = title,
        prisonId = prisonId,
        targetCompletionDate = targetCompletionDate,
        notes = notes,
      )
    }

  fun fromDomainToModel(goalDomain: Goal): GoalResponse =
    with(goalDomain) {
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
        goalNotes = emptyList(),
      )
    }

  fun fromModelToDto(archiveGoalRequest: ArchiveGoalRequest): ArchiveGoalDto =
    with(archiveGoalRequest) {
      ArchiveGoalDto(
        reference = goalReference,
        reason = toReasonToArchiveGoal(reason),
        reasonOther = reasonOther,
      )
    }

  fun fromModelToDto(unarchiveGoalRequest: UnarchiveGoalRequest): UnarchiveGoalDto =
    UnarchiveGoalDto(reference = unarchiveGoalRequest.goalReference)

  fun fromModelToDto(completeGoalRequest: CompleteGoalRequest): CompleteGoalDto =
    CompleteGoalDto(reference = completeGoalRequest.goalReference)

  fun toGoalStatus(status: GoalStatusApi): GoalStatusDomain =
    when (status) {
      GoalStatusApi.ACTIVE -> GoalStatusDomain.ACTIVE
      GoalStatusApi.COMPLETED -> GoalStatusDomain.COMPLETED
      GoalStatusApi.ARCHIVED -> GoalStatusDomain.ARCHIVED
    }

  private fun toGoalStatus(status: GoalStatusDomain): GoalStatusApi =
    when (status) {
      GoalStatusDomain.ACTIVE -> GoalStatusApi.ACTIVE
      GoalStatusDomain.COMPLETED -> GoalStatusApi.COMPLETED
      GoalStatusDomain.ARCHIVED -> GoalStatusApi.ARCHIVED
    }

  private fun toReasonToArchiveGoal(reason: ReasonToArchiveGoalApi): ReasonToArchiveGoalDomain =
    when (reason) {
      ReasonToArchiveGoalApi.PRISONER_NO_LONGER_WANTS_TO_WORK_TOWARDS_GOAL -> ReasonToArchiveGoalDomain.PRISONER_NO_LONGER_WANTS_TO_WORK_TOWARDS_GOAL
      ReasonToArchiveGoalApi.PRISONER_NO_LONGER_WANTS_TO_WORK_WITH_CIAG -> ReasonToArchiveGoalDomain.PRISONER_NO_LONGER_WANTS_TO_WORK_WITH_CIAG
      ReasonToArchiveGoalApi.SUITABLE_ACTIVITIES_NOT_AVAILABLE_IN_THIS_PRISON -> ReasonToArchiveGoalDomain.SUITABLE_ACTIVITIES_NOT_AVAILABLE_IN_THIS_PRISON
      ReasonToArchiveGoalApi.OTHER -> ReasonToArchiveGoalDomain.OTHER
    }

  private fun toReasonToArchiveGoal(reason: ReasonToArchiveGoalDomain): ReasonToArchiveGoalApi =
    when (reason) {
      ReasonToArchiveGoalDomain.PRISONER_NO_LONGER_WANTS_TO_WORK_TOWARDS_GOAL -> ReasonToArchiveGoalApi.PRISONER_NO_LONGER_WANTS_TO_WORK_TOWARDS_GOAL
      ReasonToArchiveGoalDomain.PRISONER_NO_LONGER_WANTS_TO_WORK_WITH_CIAG -> ReasonToArchiveGoalApi.PRISONER_NO_LONGER_WANTS_TO_WORK_WITH_CIAG
      ReasonToArchiveGoalDomain.SUITABLE_ACTIVITIES_NOT_AVAILABLE_IN_THIS_PRISON -> ReasonToArchiveGoalApi.SUITABLE_ACTIVITIES_NOT_AVAILABLE_IN_THIS_PRISON
      ReasonToArchiveGoalDomain.OTHER -> ReasonToArchiveGoalApi.OTHER
    }
}
