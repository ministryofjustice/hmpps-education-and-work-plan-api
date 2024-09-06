package uk.gov.justice.digital.hmpps.domain.personallearningplan

import java.util.UUID

/**
 * Thrown when a Goal cannot be created, for example because it is missing mandatory data.
 */
class InvalidGoalException(message: String) : RuntimeException(message)

/**
 * Thrown when an ActionPlan cannot be found.
 */
class ActionPlanNotFoundException(message: String) : RuntimeException(message)

/**
 * Thrown when an Attempt is made to create an ActionPlan for a prisoner who already has one.
 */
class ActionPlanAlreadyExistsException(message: String) : RuntimeException(message)

/**
 * Thrown when a specified Goal cannot be found.
 */
class GoalNotFoundException(val prisonNumber: String, val goalReference: UUID) :
  RuntimeException("Goal with reference [$goalReference] for prisoner [$prisonNumber] not found")

/**
 * Thrown when a Goal cannot be arhived/unarchived due to the current state.
 */
class InvalidGoalStateException(val prisonNumber: String, val goalReference: UUID, val status: String, val action: String) :
  RuntimeException("Could not $action goal with reference [$goalReference] for prisoner [$prisonNumber]: Goal was in state [$status] that can't be ${action}d") {

  companion object {
    const val ARCHIVE = "archive"
    const val UNARCHIVE = "unarchive"
  }
}

/**
 * A general business exception that takes a string and will have a http code of 409 CONFLICT.
 */

class BusinessException(val text: String) :
  RuntimeException(text)

/**
 * A general not found exception that takes a string and will have a http code of 404 NOT_FOUND.
 */
class NotFoundException(val text: String) :
  RuntimeException(text)
