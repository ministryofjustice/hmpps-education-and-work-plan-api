package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal

private val log = KotlinLogging.logger {}

/**
 * Service class exposing methods that implement the business rules for the Goal domain, and is how applications
 * must create and manage [Goal]s.
 *
 * Applications using [Goal]s must new up an instance of this class providing an implementation of
 * [GoalPersistenceAdapter].
 *
 * This class is deliberately final so that it cannot be subclassed, ensuring that the business rules stay within the
 * domain.
 */
class GoalService(
  private val persistenceAdapter: GoalPersistenceAdapter,
) {

  /**
   * Creates a new [Goal] for the prisoner identified by their prison number.
   */
  fun createGoal(goal: Goal, prisonNumber: String): Goal {
    log.info { "Saving Goal [${goal.reference}] for prisoner [$prisonNumber]" }
    return persistenceAdapter.createGoal(goal, prisonNumber)
  }
}
