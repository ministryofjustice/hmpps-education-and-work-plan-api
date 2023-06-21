package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal

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
   * Saves the [Goal] for the prisoner identified by their prison number.
   */
  fun saveGoal(goal: Goal, prisonNumber: String): Goal =
    persistenceAdapter.saveGoal(goal, prisonNumber)
}
