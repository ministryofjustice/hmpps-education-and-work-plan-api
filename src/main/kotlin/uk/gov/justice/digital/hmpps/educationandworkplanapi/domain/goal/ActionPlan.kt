package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal

/**
 * Represents a Prisoner's 'Action Plan', which consists of one or more [Goal]s.
 */
class ActionPlan(
  val prisonNumber: String,
  goals: List<Goal> = emptyList(),
) {

  /**
   * Returns the [Goal]s, ordered by their review date.
   */
  val goals: MutableList<Goal>
    get() = field.also { goals -> goals.sortBy { it.reviewDate } }

  init {
    this.goals = goals.toMutableList()
  }

  /**
   * Adds a [Goal] to this ActionPlan.
   */
  fun addGoal(goal: Goal) =
    goals.add(goal)

  override fun toString(): String {
    return "ActionPlan(prisonNumber='$prisonNumber', goals=$goals)"
  }
}
