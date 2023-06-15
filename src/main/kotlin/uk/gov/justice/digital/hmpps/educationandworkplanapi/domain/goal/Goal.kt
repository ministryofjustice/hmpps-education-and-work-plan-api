package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal

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
data class Goal(
  val reference: UUID,
  val title: String,
  val reviewDate: LocalDate,
  val category: GoalCategory,
  val steps: List<Step>,
  var status: GoalStatus = GoalStatus.ACTIVE,
  val notes: String?,
  val createdBy: String,
  val createdAt: Instant,
  val lastUpdatedBy: String,
  val lastUpdatedAt: Instant,
) {

  init {
    if (steps.isEmpty()) {
      throw InvalidGoalException("Cannot create Goal with reference [$reference]. At least one Step is required.")
    }
  }

  fun complete() {
    status = GoalStatus.COMPLETED
  }

  fun archive() {
    status = GoalStatus.ARCHIVED
  }
}
