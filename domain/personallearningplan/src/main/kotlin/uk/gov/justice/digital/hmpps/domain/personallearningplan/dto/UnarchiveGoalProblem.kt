package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus
import java.util.*

sealed class UnarchiveGoalProblem(open val prisonNumber: String, open val goalReference: UUID, val message: String) {
  fun errorMessage() = "Could not unarchive goal with reference [$goalReference] for prisoner [$prisonNumber]: $message"
}

data class GoalToBeUnarchivedCouldNotBeFound(override val prisonNumber: String, override val goalReference: UUID) :
  UnarchiveGoalProblem(prisonNumber, goalReference, "Not found")

data class TriedToUnarchiveAGoalInAnInvalidState(
  override val prisonNumber: String,
  override val goalReference: UUID,
  val status: GoalStatus,
) : UnarchiveGoalProblem(prisonNumber, goalReference, "Goal was in state [$status] that can't be unarchived")
