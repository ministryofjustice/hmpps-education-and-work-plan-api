package uk.gov.justice.digital.hmpps.domain.personallearningplan

import java.util.UUID

/**
 * Represents a Prisoner's 'Action Plan', which consists of one or more [Goal]s.
 */
class ActionPlan(
  val reference: UUID,
  val prisonNumber: String,
  goals: List<Goal> = emptyList(),
) {

  /**
   * Returns the [Goal]s, ordered by their created date descending (most recent ones first).
   */
  val goals: MutableList<Goal>
    get() = field.also { goals -> goals.sortByDescending { it.createdAt } }

  init {
    if (goals.isEmpty()) {
      throw InvalidActionPlanException("Cannot create ActionPlan with reference [$reference]. At least one Goal is required.")
    }
    this.goals = goals.toMutableList()
  }

  /**
   * Adds a [Goal] to this ActionPlan.
   */
  fun addGoal(goal: Goal) =
    goals.add(goal)

  override fun toString(): String {
    return "ActionPlan(reference=$reference, prisonNumber='$prisonNumber', goals=$goals)"
  }
}
