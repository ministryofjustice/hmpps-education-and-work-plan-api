package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ActionPlanEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlanNotFoundException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.ActionPlanPersistenceAdapter

@Component
class JpaActionPlanPersistenceAdapter(
  private val actionPlanRepository: ActionPlanRepository,
  private val actionPlanMapper: ActionPlanEntityMapper,
) : ActionPlanPersistenceAdapter {
  override fun getActionPlan(prisonNumber: String): ActionPlan {
    val entity = actionPlanRepository.findByPrisonNumber(prisonNumber)
      ?: throw ActionPlanNotFoundException("No Action Plan found for prisoner [$prisonNumber]")
    return actionPlanMapper.fromEntityToDomain(entity)
  }
}
