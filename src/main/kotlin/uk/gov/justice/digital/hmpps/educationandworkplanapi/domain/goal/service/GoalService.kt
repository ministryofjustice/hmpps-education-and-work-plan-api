package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalNotFoundException
import java.util.UUID

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

  /**
   * Returns a [Goal] identified by its `prisonNumber` and `goalReference`.
   * Throws [GoalNotFoundException] if the [Goal] cannot be found.
   */
  fun getGoal(prisonNumber: String, goalReference: UUID): Goal {
    log.info { "Retrieving Goal with reference [$goalReference] for prisoner [$prisonNumber]" }
    return persistenceAdapter.getGoal(prisonNumber, goalReference)
      ?: throw GoalNotFoundException(prisonNumber, goalReference).also {
        log.info { "Goal with reference [$goalReference] for prisoner [$prisonNumber] not found" }
      }
  }

  /**
   * Updates a [Goal] identified by its `prisonNumber` and `goalReference`.
   * Throws [GoalNotFoundException] if the [Goal] cannot be found.
   */
  fun updateGoal(prisonNumber: String, goalReference: UUID): Goal {
    log.info { "Updating Goal with reference [$goalReference] for prisoner [$prisonNumber]" }
    return getGoal(prisonNumber, goalReference)
    // TODO - RR-3 - implement logic to update and return updated goal; write unit tests
  }
}
