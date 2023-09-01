package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ActionPlanEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlanSummary
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.ActionPlanPersistenceAdapter

@Component
class JpaActionPlanPersistenceAdapter(
  private val actionPlanRepository: ActionPlanRepository,
  private val actionPlanMapper: ActionPlanEntityMapper,
) : ActionPlanPersistenceAdapter {

  override fun createActionPlan(actionPlan: ActionPlan): ActionPlan {
    val persistedEntity = actionPlanRepository.save(actionPlanMapper.fromDomainToEntity(actionPlan))
    return actionPlanMapper.fromEntityToDomain(persistedEntity)
  }

  override fun getActionPlan(prisonNumber: String): ActionPlan? =
    actionPlanRepository.findByPrisonNumber(prisonNumber)?.let {
      actionPlanMapper.fromEntityToDomain(it)
    }

  override fun getActionPlanSummaries(prisonNumbers: List<String>): List<ActionPlanSummary> =
    actionPlanRepository.findByPrisonNumberIn(prisonNumbers).let {
      actionPlanMapper.fromEntitySummariesToDomainSummaries(it)
    }
}
