package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlanSummary
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.CreateActionPlanDto

/**
 * Interface defining methods for how applications must create and manage a Prisoner's [ActionPlan].
 */
interface ActionPlanService {

  /**
   * Creates an [ActionPlan] for a prisoner, containing at least one or more Goals.
   */
  fun createActionPlan(createActionPlanDto: CreateActionPlanDto): ActionPlan

  /**
   * Retrieves a Prisoner's [ActionPlan] based on their prison number.
   * Returns a new [ActionPlan] if the [ActionPlan] cannot be found.
   */
  fun getActionPlan(prisonNumber: String): ActionPlan

  /**
   * Returns a list of [ActionPlanSummary]s, one for each prisoner referenced by their prison number.
   * If a specified prisoner has an [ActionPlan], their [ActionPlanSummary] will be returned in the list.
   * If a specified prisoner does not have an [ActionPlan], no corresponding [ActionPlanSummary] will be returned in the list.
   * If none of the specified prisoners have an [ActionPlan], the returned list of [ActionPlanSummary]s will be empty.
   */
  fun getActionPlanSummaries(prisonNumbers: List<String>): List<ActionPlanSummary>
}
