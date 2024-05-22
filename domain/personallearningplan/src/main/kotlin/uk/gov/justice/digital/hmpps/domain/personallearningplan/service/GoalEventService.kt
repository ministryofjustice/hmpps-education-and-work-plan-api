package uk.gov.justice.digital.hmpps.domain.personallearningplan.service

import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal

/**
 * Interface defining a series of Goal lifecycle event methods.
 */
interface GoalEventService {

  /**
   * Implementations providing custom code for when one or more [Goal]s are created.
   */
  suspend fun goalsCreated(prisonNumber: String, createdGoals: List<Goal>)

  /**
   * Implementations providing custom code for when a [Goal] is updated.
   */
  suspend fun goalUpdated(prisonNumber: String, previousGoal: Goal, updatedGoal: Goal)
}
