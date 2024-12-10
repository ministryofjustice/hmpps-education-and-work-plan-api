package uk.gov.justice.digital.hmpps.domain.personallearningplan

import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ReasonToArchiveGoal
import java.util.UUID

/**
 * Thrown when a Goal cannot be created, for example because it is missing mandatory data.
 */
class InvalidGoalException(message: String) : RuntimeException(message)

/**
 * Thrown when an Action Plan cannot be created, for example because it is missing mandatory data.
 */
class InvalidActionPlanException(message: String) : RuntimeException(message)

/**
 * Thrown when an ActionPlan cannot be found.
 */
class ActionPlanNotFoundException(prisonNumber: String) :
  RuntimeException("ActionPlan for prisoner [$prisonNumber] not found")

/**
 * Thrown when an Attempt is made to create an ActionPlan for a prisoner who already has one.
 */
class ActionPlanAlreadyExistsException(prisonNumber: String) :
  RuntimeException("An Action Plan already exists for prisoner $prisonNumber.")

/**
 * Thrown when a specified Goal cannot be found.
 */
class GoalNotFoundException(val prisonNumber: String, val goalReference: UUID) :
  RuntimeException("Goal with reference [$goalReference] for prisoner [$prisonNumber] not found")

/**
 * Thrown when a Goal cannot be arhived/unarchived due to the current state.
 */
enum class GoalAction {
  ARCHIVE,
  UNARCHIVE,
  COMPLETE,
}

class InvalidGoalStateException(
  val prisonNumber: String,
  val goalReference: UUID,
  val status: GoalStatus,
  val action: GoalAction,
) :
  RuntimeException("Could not ${action.name.lowercase()} goal with reference [$goalReference] for prisoner [$prisonNumber]: Goal was in state [$status] that can't be ${action.name.lowercase()}d")

/**
 * Thrown when a specified Goal is archived with reason other but no reason description.
 */
class NoArchiveReasonException(val goalReference: UUID, val prisonNumber: String, reason: ReasonToArchiveGoal) :
  RuntimeException(("Could not archive goal with reference [$goalReference] for prisoner [$prisonNumber]: Archive reason is $reason but no description provided"))

/**
 * Thrown when a specified Goals cannot be found for a prisoner.
 */
class PrisonerHasNoGoalsException(val prisonNumber: String) :
  RuntimeException("No goals have been created for prisoner [$prisonNumber] yet")
