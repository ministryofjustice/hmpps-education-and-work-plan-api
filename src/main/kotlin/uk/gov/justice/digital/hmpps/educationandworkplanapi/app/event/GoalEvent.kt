package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.event

import java.util.UUID

/**
 * Represents an event that has occurred to a Goal, or to an entity that's closely related to a Goal (such as one of its
 * Steps, or its parent Action Plan). Because of this, the `reference` field could belong to a Goal, Step or
 * Action Plan, however strictly speaking, `Event` may have been a better name, but it feels too vague.
 */
data class GoalEvent(
  private val reference: UUID,
  private val eventType: EventType,
)

/**
 * The goal related events that the business are interested in (for example in order to display them within a
 * Prisoner's timeline).
 */
enum class EventType {
  ACTION_PLAN_CREATED,
  GOAL_UPDATED,
  GOAL_STARTED,
  GOAL_COMPLETED,
  GOAL_ARCHIVED,
  STEP_NOT_STARTED,
  STEP_STARTED,
  STEP_COMPLETED,
}
