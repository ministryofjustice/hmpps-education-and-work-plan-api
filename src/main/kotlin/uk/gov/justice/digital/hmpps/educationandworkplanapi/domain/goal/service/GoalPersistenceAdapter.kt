package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal

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
   * Saves the [Goal] for the prisoner identified by their prison number.
   *
   * It is up to the implementation to determine whether to save a new entity or update an existing entity, as this logic
   * is not a concern of the domain, but likely to be a concern/implementation detail of the underlying persistence
   * technology.
   */
  fun saveGoal(goal: Goal, prisonNumber: String): Goal
}
