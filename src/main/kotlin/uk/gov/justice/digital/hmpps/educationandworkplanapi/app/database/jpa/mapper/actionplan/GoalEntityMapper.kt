package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.actionplan

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Step
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateStepDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ReasonToArchiveGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UpdateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.StepEntity
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus as GoalStatusDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalStatus as GoalStatusEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.ReasonToArchiveGoal as ReasonToArchiveGoalEntity

@Component
class GoalEntityMapper(
  private val stepEntityMapper: StepEntityMapper,
  private val entityListManager: GoalEntityListManager<StepEntity, Step>,
) {

  /**
   * Maps the supplied [CreateGoalDto] into a new un-persisted [GoalEntity].
   * A new reference number is generated and mapped. The JPA managed fields are not mapped.
   * This method is suitable for creating a new [GoalEntity] to be subsequently persisted to the database.
   *
   * `archiveReason` and `archiveReasonOther` are not mapped as they have no corresponding fields in the source `CreateGoalDto` instance
   * because it is not possible to create a new goal that is already in an archived state.
   */
  fun fromDtoToEntity(createGoalDto: CreateGoalDto): GoalEntity =
    with(createGoalDto) {
      GoalEntity(
        reference = UUID.randomUUID(),
        createdAtPrison = createGoalDto.prisonId,
        updatedAtPrison = createGoalDto.prisonId,
        title = createGoalDto.title,
        targetCompletionDate = createGoalDto.targetCompletionDate,
        status = toGoalStatus(createGoalDto.status),
        notes = createGoalDto.notes,
        steps = mutableListOf(),
      ).also {
        addNewStepsToEntity(steps, it)
      }
    }

  /**
   * Maps the supplied [GoalEntity] into the domain [Goal].
   */
  fun fromEntityToDomain(goalEntity: GoalEntity): Goal =
    with(goalEntity) {
      Goal(
        reference = reference,
        title = title,
        targetCompletionDate = targetCompletionDate,
        status = toGoalStatus(status),
        notes = notes,
        createdBy = createdBy,
        createdAt = createdAt,
        createdAtPrison = createdAtPrison,
        lastUpdatedBy = updatedBy,
        lastUpdatedAt = updatedAt,
        lastUpdatedAtPrison = updatedAtPrison,
        archiveReason = archiveReason?.let { archiveReasonFromDomainToEntity(it) },
        archiveReasonOther = archiveReasonOther,
        steps = steps.map { stepEntityMapper.fromEntityToDomain(it) },
      )
    }

  /**
   * Updates the supplied [GoalEntity] with fields from the supplied [UpdateGoalDto]. The updated [GoalEntity] can then be
   * persisted to the database.
   *
   * `status`, `archiveReason` and `archiveReasonOther` are not mapped as they have no corresponding fields in the source `UpdateGoalDto` instance
   * because it is not possible to either update a goal's status or update an archived goal via the Update Goal operation.
   */
  fun updateEntityFromDto(goalEntity: GoalEntity, updatedGoalDto: UpdateGoalDto) =
    with(goalEntity) {
      updatedAtPrison = updatedGoalDto.prisonId
      title = updatedGoalDto.title
      targetCompletionDate = updatedGoalDto.targetCompletionDate
      notes = updatedGoalDto.notes
      steps = updateSteps(this, updatedGoalDto)
    }

  fun archiveReasonFromDomainToEntity(reason: ReasonToArchiveGoal): ReasonToArchiveGoalEntity =
    when (reason) {
      ReasonToArchiveGoal.PRISONER_NO_LONGER_WANTS_TO_WORK_TOWARDS_GOAL -> ReasonToArchiveGoalEntity.PRISONER_NO_LONGER_WANTS_TO_WORK_TOWARDS_GOAL
      ReasonToArchiveGoal.PRISONER_NO_LONGER_WANTS_TO_WORK_WITH_CIAG -> ReasonToArchiveGoalEntity.PRISONER_NO_LONGER_WANTS_TO_WORK_WITH_CIAG
      ReasonToArchiveGoal.SUITABLE_ACTIVITIES_NOT_AVAILABLE_IN_THIS_PRISON -> ReasonToArchiveGoalEntity.SUITABLE_ACTIVITIES_NOT_AVAILABLE_IN_THIS_PRISON
      ReasonToArchiveGoal.OTHER -> ReasonToArchiveGoalEntity.OTHER
    }

  fun archiveReasonFromDomainToEntity(reason: ReasonToArchiveGoalEntity): ReasonToArchiveGoal =
    when (reason) {
      ReasonToArchiveGoalEntity.PRISONER_NO_LONGER_WANTS_TO_WORK_TOWARDS_GOAL -> ReasonToArchiveGoal.PRISONER_NO_LONGER_WANTS_TO_WORK_TOWARDS_GOAL
      ReasonToArchiveGoalEntity.PRISONER_NO_LONGER_WANTS_TO_WORK_WITH_CIAG -> ReasonToArchiveGoal.PRISONER_NO_LONGER_WANTS_TO_WORK_WITH_CIAG
      ReasonToArchiveGoalEntity.SUITABLE_ACTIVITIES_NOT_AVAILABLE_IN_THIS_PRISON -> ReasonToArchiveGoal.SUITABLE_ACTIVITIES_NOT_AVAILABLE_IN_THIS_PRISON
      ReasonToArchiveGoalEntity.OTHER -> ReasonToArchiveGoal.OTHER
    }

  private fun updateSteps(entity: GoalEntity, dto: UpdateGoalDto): MutableList<StepEntity> {
    val existingSteps = entity.steps
    val updatedSteps = dto.steps.map { stepEntityMapper.fromDtoToDomain(it) }

    entityListManager.updateExisting(existingSteps, updatedSteps, stepEntityMapper)
    entityListManager.addNew(entity, existingSteps, updatedSteps, stepEntityMapper)
    entityListManager.deleteRemoved(existingSteps, updatedSteps)

    existingSteps.sortBy { it.sequenceNumber }

    return existingSteps
  }

  private fun addNewStepsToEntity(steps: List<CreateStepDto>, entity: GoalEntity) {
    steps.forEach {
      entity.addChild(
        stepEntityMapper.fromDtoToEntity(it),
        entity.steps,
      )
    }
  }

  private fun toGoalStatus(goalStatus: GoalStatusDomain): GoalStatusEntity =
    when (goalStatus) {
      GoalStatusDomain.ACTIVE -> GoalStatusEntity.ACTIVE
      GoalStatusDomain.ARCHIVED -> GoalStatusEntity.ARCHIVED
      GoalStatusDomain.COMPLETED -> GoalStatusEntity.COMPLETED
    }

  private fun toGoalStatus(goalStatus: GoalStatusEntity): GoalStatusDomain =
    when (goalStatus) {
      GoalStatusEntity.ACTIVE -> GoalStatusDomain.ACTIVE
      GoalStatusEntity.ARCHIVED -> GoalStatusDomain.ARCHIVED
      GoalStatusEntity.COMPLETED -> GoalStatusDomain.COMPLETED
    }
}
