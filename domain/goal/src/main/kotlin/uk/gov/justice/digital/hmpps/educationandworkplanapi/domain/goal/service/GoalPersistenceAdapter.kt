package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import java.util.UUID

/**
 * Persistence Adapter for [Goal] instances.
 *
 * Implementations should use the underlying persistence of the application in question, eg: JPA, Mongo, Dynamo, Redis etc
 *
 * Implementations should not throw exceptions. These are not part of the interface and are not checked or handled by
 * [GoalService].
 */
interface GoalPersistenceAdapter {

  /**
   * Creates a new [Goal] for the prisoner identified by their prison number.
   */
  fun createGoal(goal: Goal, prisonNumber: String): Goal

  /**
   * Returns a [Goal] identified by its `prisonNumber` and `goalReference` if found, otherwise `null`.
   */
  fun getGoal(prisonNumber: String, goalReference: UUID): Goal?

  /**
   * Updates a [Goal] identified by its `prisonNumber` and `goalReference`
   */
  fun updateGoal(prisonNumber: String, goalReference: UUID, updatedGoal: Goal): Goal?
}
