package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus
import java.util.*

sealed class UnarchiveGoalResult {

  data class GoalToBeUnarchivedCouldNotBeFound(val prisonNumber: String, val goalReference: UUID) :
    UnarchiveGoalResult() {
    fun errorMessage() =
      "Could not unarchive goal with reference [$goalReference] for prisoner [$prisonNumber]: Not found"
  }

  data class TriedToUnarchiveAGoalInAnInvalidState(
    val prisonNumber: String,
    val goalReference: UUID,
    val status: GoalStatus,
  ) : UnarchiveGoalResult() {
    fun errorMessage() =
      "Could not unarchive goal with reference [$goalReference] for prisoner [$prisonNumber]: Goal was in state [$status] that can't be unarchived"
  }

  data class UnArchivedGoalSuccessfully(val updatedGoal: Goal) : UnarchiveGoalResult()
}
