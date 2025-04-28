package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlanNotFoundException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CompleteGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ReasonToArchiveGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UnarchiveGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UpdateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.GoalPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.actionplan.GoalEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.GoalRepository
import java.util.*

private val log = KotlinLogging.logger {}

@Component
class JpaGoalPersistenceAdapter(
  private val goalRepository: GoalRepository,
  private val actionPlanRepository: ActionPlanRepository,
  private val goalMapper: GoalEntityMapper,
) : GoalPersistenceAdapter {

  @Transactional
  override fun createGoals(prisonNumber: String, createGoalDtos: List<CreateGoalDto>): List<Goal> {
    val actionPlanEntity = actionPlanRepository.findByPrisonNumber(prisonNumber)
      ?: throw ActionPlanNotFoundException(prisonNumber)

    val goalEntities = createGoalDtos.map { goalMapper.fromDtoToEntity(it) }
    with(actionPlanEntity) {
      goalEntities.forEach { addGoal(it) }
      actionPlanRepository.save(this)
    }

    val goalEntityMap = goalEntities.associateBy { it.reference }
    val persistedGoals = actionPlanEntity.goals!!.filter { goalEntityMap.containsKey(it.reference) }

    // Set the notes from the corresponding goalEntity
    persistedGoals.forEach { persistedGoal ->
      val correspondingGoalEntity = goalEntityMap[persistedGoal.reference]
      if (correspondingGoalEntity != null) {
        persistedGoal.notes = correspondingGoalEntity.notes
      }
    }

    return persistedGoals.map { goalMapper.fromEntityToDomain(it) }
  }

  @Transactional(readOnly = true)
  override fun getGoals(prisonNumber: String): List<Goal>? = actionPlanRepository
    .findByPrisonNumber(prisonNumber)?.goals
    ?.map { goalMapper.fromEntityToDomain(it) }

  @Transactional(readOnly = true)
  override fun getGoal(prisonNumber: String, goalReference: UUID): Goal? {
    val goalEntity = getGoalEntityByPrisonNumberAndGoalReference(prisonNumber, goalReference)

    return if (goalEntity != null) {
      goalMapper.fromEntityToDomain(goalEntity)
    } else {
      null
    }
  }

  @Transactional
  override fun updateGoal(prisonNumber: String, updatedGoalDto: UpdateGoalDto): Goal? {
    val goalEntity = getGoalEntityByPrisonNumberAndGoalReference(prisonNumber, updatedGoalDto.reference)
    return if (goalEntity != null) {
      goalMapper.updateEntityFromDto(goalEntity, updatedGoalDto)
      val persistedEntity = goalRepository.saveAndFlush(goalEntity)
      goalMapper.fromEntityToDomain(persistedEntity)
    } else {
      null
    }
  }

  override fun archiveGoal(prisonNumber: String, archiveGoalDto: ArchiveGoalDto): Goal? = updateStatus(
    prisonNumber,
    archiveGoalDto.reference,
    GoalStatus.ARCHIVED,
    archiveGoalDto.reason,
    archiveGoalDto.reasonOther,
    archiveGoalDto.prisonId,
  )

  override fun completeGoal(prisonNumber: String, completeGoalDto: CompleteGoalDto): Goal? = getGoalEntityByPrisonNumberAndGoalReference(prisonNumber, completeGoalDto.reference)?.let { goalEntity ->
    goalEntity.apply {
      status = GoalStatus.COMPLETED
      updatedAtPrison = completeGoalDto.prisonId
    }
    goalEntity.steps.forEach { it.apply { it.status = StepStatus.COMPLETE } }
    val persistedEntity = goalRepository.saveAndFlush(goalEntity)
    goalMapper.fromEntityToDomain(persistedEntity)
  }

  override fun unarchiveGoal(prisonNumber: String, unarchiveGoalDto: UnarchiveGoalDto): Goal? = updateStatus(
    prisonNumber,
    unarchiveGoalDto.reference,
    GoalStatus.ACTIVE,
    prisonId = unarchiveGoalDto.prisonId,
  )

  private fun updateStatus(
    prisonNumber: String,
    goalReference: UUID,
    goalStatus: GoalStatus,
    reason: ReasonToArchiveGoal? = null,
    reasonOther: String? = null,
    prisonId: String,
  ): Goal? = getGoalEntityByPrisonNumberAndGoalReference(prisonNumber, goalReference)?.let { goalEntity ->
    goalEntity.apply {
      status = goalStatus
      archiveReason = reason?.let { goalMapper.archiveReasonFromDomainToEntity(it) }
      archiveReasonOther = reasonOther
      updatedAtPrison = prisonId
    }
    val persistedEntity = goalRepository.saveAndFlush(goalEntity)
    goalMapper.fromEntityToDomain(persistedEntity)
  }

  private fun getGoalEntityByPrisonNumberAndGoalReference(prisonNumber: String, goalReference: UUID): GoalEntity? = actionPlanRepository.findByPrisonNumber(prisonNumber)?.goals?.firstOrNull { goal -> goal.reference == goalReference }
}
