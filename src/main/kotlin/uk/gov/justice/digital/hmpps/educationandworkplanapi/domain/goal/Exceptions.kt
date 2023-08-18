package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal

import java.util.UUID

/**
 * Thrown when an Action Plan cannot be created, for example because it is missing mandatory data.
 */
class InvalidActionPlanException(message: String) : RuntimeException(message)

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
