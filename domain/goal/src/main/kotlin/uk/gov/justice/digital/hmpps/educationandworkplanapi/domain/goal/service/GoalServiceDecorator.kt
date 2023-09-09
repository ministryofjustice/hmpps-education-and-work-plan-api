package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.CreateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.UpdateGoalDto
import java.util.UUID

/**
 * Interface defining a series of "before" and "after" methods to decorate the [GoalService] method behaviour with.
 */
interface GoalServiceDecorator {

  /**
   * Custom code invoked before a [Goal] is created.
   */
  fun beforeCreateGoal(prisonNumber: String, createGoalDto: CreateGoalDto)

  /**
   * Custom code invoked after a [Goal] is created.
   */
  fun afterCreateGoal(prisonNumber: String, createGoalDto: CreateGoalDto, createdGoal: Goal)

  /**
   * Custom code invoked before a [Goal] is returned.
   */
  fun beforeGetGoal(prisonNumber: String, goalReference: UUID)

  /**
   * Custom code invoked after a [Goal] is returned.
   */
  fun afterGetGoal(prisonNumber: String, goalReference: UUID, retrievedGoal: Goal)

  /**
   * Custom code invoked before a [Goal] is updated.
   */
  fun beforeUpdateGoal(prisonNumber: String, updatedGoalDto: UpdateGoalDto)

  /**
   * Custom code invoked after a [Goal] is updated.
   */
  fun afterUpdateGoal(prisonNumber: String, updatedGoalDto: UpdateGoalDto, updatedGoal: Goal)
}

/**
 * Default No-op implementation of [GoalServiceDecorator], suitable to be overridden.
 */
open class NoopGoalServiceDecorator : GoalServiceDecorator {

  override fun beforeCreateGoal(prisonNumber: String, createGoalDto: CreateGoalDto) {}

  override fun afterCreateGoal(prisonNumber: String, createGoalDto: CreateGoalDto, createdGoal: Goal) {}

  override fun beforeGetGoal(prisonNumber: String, goalReference: UUID) {}

  override fun afterGetGoal(prisonNumber: String, goalReference: UUID, retrievedGoal: Goal) {}

  override fun beforeUpdateGoal(prisonNumber: String, updatedGoalDto: UpdateGoalDto) {}

  override fun afterUpdateGoal(prisonNumber: String, updatedGoalDto: UpdateGoalDto, updatedGoal: Goal) {}
}
