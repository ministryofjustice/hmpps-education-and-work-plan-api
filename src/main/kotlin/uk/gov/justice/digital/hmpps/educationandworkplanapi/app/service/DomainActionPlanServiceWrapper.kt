package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlanSummary
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.CreateActionPlanDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.ActionPlanService

/**
 * Implementation of [ActionPlanService] that wraps the default domain Action Plan Service methods to allow for
 * invoking additional behaviours.
 */
@Component("domainActionPlanServiceWrapper")
class DomainActionPlanServiceWrapper(
  @Qualifier("defaultDomainActionPlanService") private val wrappedActionPlanService: ActionPlanService,
) : ActionPlanService {
  override fun createActionPlan(createActionPlanDto: CreateActionPlanDto): ActionPlan =
    wrappedActionPlanService.createActionPlan(createActionPlanDto)

  override fun getActionPlan(prisonNumber: String): ActionPlan =
    wrappedActionPlanService.getActionPlan(prisonNumber)

  override fun getActionPlanSummaries(prisonNumbers: List<String>): List<ActionPlanSummary> =
    wrappedActionPlanService.getActionPlanSummaries(prisonNumbers)
}
