package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlan

private val log = KotlinLogging.logger {}

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
   * Returns a new [ActionPlan] if the [ActionPlan] cannot be found.
   */
  fun getActionPlan(prisonNumber: String): ActionPlan {
    log.debug { "Retrieving Action Plan for prisoner [$prisonNumber]" }
    return persistenceAdapter.getActionPlan(prisonNumber)
      ?: ActionPlan(prisonNumber, emptyList())
  }
}
