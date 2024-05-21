package uk.gov.justice.digital.hmpps.domain.personallearningplan.service

import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlan

/**
 * Interface for defining [ActionPlan] related lifecycle events.
 */
interface ActionPlanEventService {

  /**
   * Implementations providing custom code for when an [ActionPlan] is created.
   */
  suspend fun actionPlanCreated(actionPlan: ActionPlan)
}
