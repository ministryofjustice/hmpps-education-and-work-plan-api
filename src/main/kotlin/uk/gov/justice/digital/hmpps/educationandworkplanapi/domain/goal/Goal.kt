package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.ARCHIVED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.COMPLETED
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Represents a prisoner's Goal in their Education and Work Plan (or so-called "Action Plan").
 *
 * The goals have different categories and may be personal development based (e.g. improving communication) or related
 * to a future job/career and consist of one or more [Step Steps] (at least one).
 *
 * Each goal has its own lifecycle (such as active or completed) and a prisoner can have many goals at the same time
 * (e.g. more than one active goal), thereby creating a "Action Plan".
 */
class Goal(
  val reference: UUID,
  val title: String,
  val reviewDate: LocalDate?,
  val category: GoalCategory,
  var status: GoalStatus = GoalStatus.ACTIVE,
  val notes: String? = null,
  val createdBy: String?,
  val createdByDisplayName: String?,
  val createdAt: Instant?,
  val lastUpdatedBy: String?,
  val lastUpdatedByDisplayName: String?,
  val lastUpdatedAt: Instant?,
  steps: List<Step>,
) {

  /**
   * Returns the Goal's [Step]s, ordered by their sequence number (ascending).
   */
  val steps: MutableList<Step>
    get() = field.also { steps -> steps.sortBy { it.sequenceNumber } }

  init {
    if (steps.isEmpty()) {
      throw InvalidGoalException("Cannot create Goal with reference [$reference]. At least one Step is required.")
    }
    this.steps = steps.toMutableList()
  }

  /**
   * Adds a [Step] to this Goal.
   */
  fun addStep(step: Step) =
    steps.add(step)

  fun complete() {
    status = COMPLETED
  }

  fun archive() {
    status = ARCHIVED
  }

  override fun toString(): String {
    return "Goal(reference=$reference, title='$title', reviewDate=$reviewDate, status=$status, createdBy='$createdBy', createdAt=$createdAt, lastUpdatedBy='$lastUpdatedBy', lastUpdatedAt=$lastUpdatedAt, steps=$steps)"
  }
}
