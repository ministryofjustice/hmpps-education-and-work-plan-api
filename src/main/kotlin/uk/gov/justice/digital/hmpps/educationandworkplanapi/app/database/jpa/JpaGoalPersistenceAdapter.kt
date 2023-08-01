package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.ActionPlanEntity.Companion.newActionPlanForPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.GoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GoalEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalPersistenceAdapter
import java.util.UUID

private val log = KotlinLogging.logger {}

@Component
class JpaGoalPersistenceAdapter(
  private val actionPlanRepository: ActionPlanRepository,
  private val goalMapper: GoalEntityMapper,
) : GoalPersistenceAdapter {

  @Transactional
  override fun createGoal(goal: Goal, prisonNumber: String): Goal {
    var actionPlanEntity = actionPlanRepository.findByPrisonNumber(prisonNumber)
    if (actionPlanEntity == null) {
      log.info { "Creating new Action Plan for prisoner [$prisonNumber]" }
      actionPlanEntity = newActionPlanForPrisoner(prisonNumber)
    }

    val goalEntity = goalMapper.fromDomainToEntity(goal)

    with(actionPlanEntity) {
      addGoal(goalEntity)
      actionPlanRepository.saveAndFlush(this)
    }

    return goalMapper.fromEntityToDomain(goalEntity)
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
  override fun updateGoal(prisonNumber: String, goalReference: UUID, updatedGoal: Goal): Goal? {
    val goalEntity = getGoalEntityByPrisonNumberAndGoalReference(prisonNumber, goalReference)
    return if (goalEntity != null) {
      goalMapper.updateEntityFromDomain(goalEntity, updatedGoal)
      goalMapper.fromEntityToDomain(goalEntity)
    } else {
      null
    }
  }

  private fun getGoalEntityByPrisonNumberAndGoalReference(prisonNumber: String, goalReference: UUID): GoalEntity? =
    actionPlanRepository.findByPrisonNumber(prisonNumber)?.goals?.firstOrNull { goal -> goal.reference == goalReference }
}
