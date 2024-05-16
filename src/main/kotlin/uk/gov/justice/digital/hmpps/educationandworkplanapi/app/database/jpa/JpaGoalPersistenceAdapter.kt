package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlanNotFoundException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UpdateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.GoalPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.actionplan.GoalEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.GoalRepository
import java.util.UUID

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
      ?: throw ActionPlanNotFoundException("Unable to find ActionPlan for prisoner [$prisonNumber]")

    val goalEntities = createGoalDtos.map { goalMapper.fromDtoToEntity(it) }
    with(actionPlanEntity) {
      goalEntities.forEach { addGoal(it) }
      actionPlanRepository.save(this)
    }

    // use the persisted entities with the populated JPA fields, rather than the non persisted entity reference above
    val persisted = goalEntities.map { goalEntity -> actionPlanEntity.goals!!.first { it.reference == goalEntity.reference } }
    return persisted.map { goalMapper.fromEntityToDomain(it) }
  }

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

  private fun getGoalEntityByPrisonNumberAndGoalReference(prisonNumber: String, goalReference: UUID): GoalEntity? =
    actionPlanRepository.findByPrisonNumber(prisonNumber)?.goals?.firstOrNull { goal -> goal.reference == goalReference }
}
