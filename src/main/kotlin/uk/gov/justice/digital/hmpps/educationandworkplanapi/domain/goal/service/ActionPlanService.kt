package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlanNotFoundException

/**
 * Service class exposing methods that implement the business rules for a Prisoner's [ActionPlan].
 *
 * Applications using [ActionPlan]s must new up an instance of this class providing an implementation of
 * [ActionPlanPersistenceAdapter].
 *
 * This class is deliberately final so that it cannot be subclassed, ensuring that the business rules stay within the
 * domain.
 */
class ActionPlanService(
  private val persistenceAdapter: ActionPlanPersistenceAdapter,
) {

  /**
   * Retrieves a Prisoner's [ActionPlan] based on their prison number.
   * Throws [ActionPlanNotFoundException] if the [ActionPlan] cannot be found.
   */
  fun getActionPlan(prisonNumber: String): ActionPlan =
    persistenceAdapter.getActionPlan(prisonNumber)
      ?: throw ActionPlanNotFoundException("No Action Plan found for prisoner [$prisonNumber]")
}
