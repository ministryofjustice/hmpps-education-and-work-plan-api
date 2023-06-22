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
  override fun createGoal(goal: Goal, prisonNumber: String): Goal {
    val actionPlanEntity = actionPlanRepository.findByPrisonNumber(prisonNumber)
      ?: newActionPlanForPrisoner(prisonNumber)

    val goalEntity = goalMapper.fromDomainToEntity(goal)

    with(actionPlanEntity) {
      addGoal(goalEntity)
      actionPlanRepository.saveAndFlush(this)
    }

    return goalMapper.fromEntityToDomain(goalEntity)
  }
}
