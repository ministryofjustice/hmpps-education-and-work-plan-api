package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.ActionPlanEntity.Companion.newActionPlanForPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GoalEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalPersistenceAdapter

@Component
class JpaGoalPersistenceAdapter(
  private val actionPlanRepository: ActionPlanRepository,
  private val goalMapper: GoalEntityMapper,
) : GoalPersistenceAdapter {

  @Transactional
  override fun saveGoal(goal: Goal, prisonNumber: String): Goal {
    var actionPlanEntity = actionPlanRepository.findByPrisonNumber(prisonNumber)
      ?: newActionPlanForPrisoner(prisonNumber)

    val goalEntity = actionPlanEntity.getGoalByReference(goal.reference)
    if (goalEntity != null) {
      // Goal already exists - we need to update the existing Goal entity
      goalMapper.updateEntityFromDomain(goalEntity, goal)
    } else {
      // Goal does not already exist - map to a new Goal entity and add to the action plan
      val newGoal = goalMapper.fromDomainToEntity(goal)
      actionPlanEntity.addGoal(newGoal)
    }

    actionPlanEntity = actionPlanRepository.saveAndFlush(actionPlanEntity)
    val savedGoalEntity = actionPlanEntity.getGoalByReference(goal.reference)!!
    return goalMapper.fromEntityToDomain(savedGoalEntity)
  }
}
