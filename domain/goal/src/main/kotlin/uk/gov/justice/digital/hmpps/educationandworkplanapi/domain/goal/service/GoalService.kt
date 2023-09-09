package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalNotFoundException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.CreateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.UpdateGoalDto
import java.util.UUID

/**
 * Interface defining methods for how applications must create and manage [Goal]s.
 */
interface GoalService {

  /**
   * Creates a new [Goal] for the prisoner identified by their prison number, with the data in the specified [CreateGoalDto].
   * Returns the created [Goal]
   */
  fun createGoal(prisonNumber: String, createGoalDto: CreateGoalDto): Goal

  /**
   * Returns a [Goal] identified by its `prisonNumber` and `goalReference`.
   * Throws [GoalNotFoundException] if the [Goal] cannot be found.
   */
  fun getGoal(prisonNumber: String, goalReference: UUID): Goal

  /**
   * Updates a [Goal], identified by its `prisonNumber` and `goalReference`, from the specified [UpdateGoalDto].
   * Returns the updated [Goal].
   * Throws [GoalNotFoundException] if the [Goal] to be updated cannot be found.
   */
  fun updateGoal(prisonNumber: String, updatedGoalDto: UpdateGoalDto): Goal
}
