package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal

/**
 * Interface defining a series of Goal lifecycle event methods.
 */
interface GoalEventService {

  /**
   * Implementations providing custom code for when a [Goal] is created.
   */
  fun goalCreated(prisonNumber: String, createdGoal: Goal)

  /**
   * Implementations providing custom code for when a [Goal] is updated.
   */
  fun goalUpdated(prisonNumber: String, previousGoal: Goal, updatedGoal: Goal)
}
