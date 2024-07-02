package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus
import java.util.*

sealed class ArchiveGoalResult {
  data class GoalNotFound(val prisonNumber: String, val goalReference: UUID) : ArchiveGoalResult() {
    fun errorMessage() =
      "Could not archive goal with reference [$goalReference] for prisoner [$prisonNumber]: Not found"
  }

  data class NoDescriptionProvidedForOther(val prisonNumber: String, val goalReference: UUID) :
    ArchiveGoalResult() {
    fun errorMessage() =
      "Could not archive goal with reference [$goalReference] for prisoner [$prisonNumber]: Archive reason is ${ReasonToArchiveGoal.OTHER} but no description provided"
  }

  data class GoalInAnInvalidState(
    val prisonNumber: String,
    val goalReference: UUID,
    val status: GoalStatus,
  ) : ArchiveGoalResult() {
    fun errorMessage() =
      "Could not archive goal with reference [$goalReference] for prisoner [$prisonNumber]: Goal was in state [$status] that can't be archived"
  }

  data class Success(val updatedGoal: Goal) : ArchiveGoalResult()
}
