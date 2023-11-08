package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.actionplan.ActionPlanEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlanSummary
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.CreateActionPlanDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.ActionPlanPersistenceAdapter

@Component
class JpaActionPlanPersistenceAdapter(
  private val actionPlanRepository: ActionPlanRepository,
  private val actionPlanMapper: ActionPlanEntityMapper,
) : ActionPlanPersistenceAdapter {

  @Transactional
  override fun createActionPlan(createActionPlanDto: CreateActionPlanDto): ActionPlan {
    val persistedEntity = actionPlanRepository.saveAndFlush(actionPlanMapper.fromDtoToEntity(createActionPlanDto))
    return actionPlanMapper.fromEntityToDomain(persistedEntity)
  }

  @Transactional(readOnly = true)
  override fun getActionPlan(prisonNumber: String): ActionPlan? =
    actionPlanRepository.findByPrisonNumber(prisonNumber)?.let {
      actionPlanMapper.fromEntityToDomain(it)
    }

  @Transactional(readOnly = true)
  override fun getActionPlanSummaries(prisonNumbers: List<String>): List<ActionPlanSummary> =
    actionPlanRepository.findByPrisonNumberIn(prisonNumbers).let {
      actionPlanMapper.fromEntitySummariesToDomainSummaries(it)
    }
}
