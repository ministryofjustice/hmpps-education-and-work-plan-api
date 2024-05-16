package uk.gov.justice.digital.hmpps.domain.goal.service

import uk.gov.justice.digital.hmpps.domain.goal.ActionPlan

/**
 * Interface for defining [ActionPlan] related lifecycle events.
 */
interface ActionPlanEventService {

  /**
   * Implementations providing custom code for when an [ActionPlan] is created.
   */
  fun actionPlanCreated(actionPlan: ActionPlan)
}
