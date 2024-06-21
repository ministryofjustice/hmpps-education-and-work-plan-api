package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus
import java.util.*

sealed class ArchiveGoalProblem(open val prisonNumber: String, open val goalReference: UUID, val message: String) {
  fun errorMessage() = "Could not archive goal with reference [$goalReference] for prisoner [$prisonNumber]: $message"
}

data class GoalToBeArchivedCouldNotBeFound(override val prisonNumber: String, override val goalReference: UUID) :
  ArchiveGoalProblem(prisonNumber, goalReference, "Not found")

data class ArchiveReasonIsOtherButNoDescriptionProvided(
  override val prisonNumber: String,
  override val goalReference: UUID,
) : ArchiveGoalProblem(
  prisonNumber,
  goalReference,
  "Archive reason is ${ReasonToArchiveGoal.OTHER} but no description provided",
)

data class TriedToArchiveAGoalInAnInvalidState(
  override val prisonNumber: String,
  override val goalReference: UUID,
  val status: GoalStatus,
) : ArchiveGoalProblem(prisonNumber, goalReference, "Goal was in state [$status] that can't be archived")
