package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.GoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GoalEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlanNotFoundException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.CreateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.UpdateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalPersistenceAdapter
import java.util.UUID

private val log = KotlinLogging.logger {}

@Component
class JpaGoalPersistenceAdapter(
  private val actionPlanRepository: ActionPlanRepository,
  private val goalMapper: GoalEntityMapper,
) : GoalPersistenceAdapter {

  @Transactional
  override fun createGoal(prisonNumber: String, createGoalDto: CreateGoalDto): Goal {
    val actionPlanEntity = actionPlanRepository.findByPrisonNumber(prisonNumber)
      ?: throw ActionPlanNotFoundException("Unable to find ActionPlan for prisoner [$prisonNumber]")

    val goalEntity = goalMapper.fromDtoToEntity(createGoalDto)
    with(actionPlanEntity) {
      addGoal(goalEntity)
      actionPlanRepository.saveAndFlush(this)
    }

    // use the persisted entity with the populated JPA fields, rather than the non persisted entity reference above
    val persisted = actionPlanEntity.goals!!.first { it.reference == goalEntity.reference }
    return goalMapper.fromEntityToDomain(persisted)
  }

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
      goalMapper.fromEntityToDomain(goalEntity)
    } else {
      null
    }
  }

  private fun getGoalEntityByPrisonNumberAndGoalReference(prisonNumber: String, goalReference: UUID): GoalEntity? =
    actionPlanRepository.findByPrisonNumber(prisonNumber)?.goals?.firstOrNull { goal -> goal.reference == goalReference }
}
