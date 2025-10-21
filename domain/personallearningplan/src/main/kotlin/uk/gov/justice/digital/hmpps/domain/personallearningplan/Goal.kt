package uk.gov.justice.digital.hmpps.domain.personallearningplan

import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus.ARCHIVED
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus.COMPLETED
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ReasonToArchiveGoal
import java.time.Instant
import java.time.LocalDate
import java.util.*

/**
 * Represents a prisoner's Goal in their Education and Work Plan (or so-called "Action Plan").
 *
 * The goals vary significantly and may be personal development based (e.g. improving communication) or related
 * to a future job/career and consist of one or more [Step Steps] (at least one).
 *
 * Each goal has its own lifecycle (such as active or completed) and a prisoner can have many goals at the same time
 * (e.g. more than one active goal), thereby creating an "Action Plan".
 */
class Goal(
  val reference: UUID,
  val title: String,
  val targetCompletionDate: LocalDate,
  var status: GoalStatus = GoalStatus.ACTIVE,
  var notes: String? = null,
  val createdBy: String?,
  val createdAt: Instant?,
  val createdAtPrison: String,
  val lastUpdatedBy: String?,
  val lastUpdatedAt: Instant?,
  val lastUpdatedAtPrison: String,
  val archiveReason: ReasonToArchiveGoal?,
  val archiveReasonOther: String?,
  steps: List<Step>,
) {

  /**
   * Returns the Goal's [Step]s, ordered by their sequence number (ascending).
   * The sequence number is enforced to be sequential starting from 1 with no gaps.
   */
  val steps: MutableList<Step>
    get() = field.also { steps ->
      val sortedSequencedSteps = steps
        .sortedBy { it.sequenceNumber } // sort by current sequence number (that may include gaps if steps have been removed)
        .mapIndexed { idx, step -> step.copy(sequenceNumber = idx + 1) } // map each step to a copy of itself with the sequential sequence number
      steps.clear() // empty and replace the steps list with the new sorted sequenced steps
      steps.addAll(sortedSequencedSteps)
    }

  init {
    if (steps.isEmpty()) {
      throw InvalidGoalException("Cannot create Goal with reference [$reference]. At least one Step is required.")
    }
    this.steps = steps.toMutableList()
  }

  /**
   * Adds a [Step] to this Goal, observing its sequenceNumber relative to the existing list of [Step]s
   */
  fun addStep(step: Step) = steps.apply {
    // Current steps list is ordered with sequential sequenceNumbers by virtue of steps getter
    // Work out where to insert this new step based on it's sequenceNumber
    val insertionIndex =
      if (step.sequenceNumber > this.size) {
        this.size
      } else if (step.sequenceNumber < 1) {
        0
      } else {
        step.sequenceNumber - 1
      }
    add(insertionIndex, step)
  }

  fun complete() {
    status = COMPLETED
  }

  fun archive() {
    status = ARCHIVED
  }

  override fun toString(): String = "Goal(reference=$reference, title='$title', targetCompletionDate=$targetCompletionDate, status=$status, createdBy='$createdBy', createdAt=$createdAt, lastUpdatedBy='$lastUpdatedBy', lastUpdatedAt=$lastUpdatedAt, steps=$steps)"
}

enum class GoalStatus {
  ACTIVE,
  COMPLETED,
  ARCHIVED,
}
