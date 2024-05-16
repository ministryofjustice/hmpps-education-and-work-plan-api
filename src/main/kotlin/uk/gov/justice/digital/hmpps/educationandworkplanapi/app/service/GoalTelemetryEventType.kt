package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import uk.gov.justice.digital.hmpps.domain.goal.Goal
import java.util.UUID

/**
 * An enumeration of the types of Goal events that can be sent to the Telemetry Service, where each item has:
 *   * a text field `value` that is the name of the customEvent recorded in App Insights.
 *   * a function `customDimensions` that returns the customDimensions that are recorded with the App Insights event.
 */
enum class GoalTelemetryEventType(val value: String, val customDimensions: (goal: Goal, correlationId: UUID) -> Map<String, String>) {
  GOAL_CREATED(
    "goal-created",
    { goal, correlationId ->
      mapOf(
        "correlationId" to correlationId.toString(),
        "status" to goal.status.name,
        "stepCount" to goal.steps.size.toString(),
        "reference" to goal.reference.toString(),
        "notesCharacterCount" to (goal.notes?.length ?: 0).toString(),
      )
    },
  ),

  GOAL_UPDATED(
    "goal-updated",
    { goal, correlationId ->
      mapOf(
        "correlationId" to correlationId.toString(),
        "reference" to goal.reference.toString(),
        "notesCharacterCount" to (goal.notes?.length ?: 0).toString(),
      )
    },
  ),

  GOAL_STARTED("goal-started", { goal, correlationId -> emptyMap() }),

  GOAL_COMPLETED("goal-completed", { goal, correlationId -> emptyMap() }),

  GOAL_ARCHIVED("goal-archived", { goal, correlationId -> emptyMap() }),

  STEP_UPDATED("step-updated", { goal, correlationId -> emptyMap() }),

  STEP_NOT_STARTED("step-not-started", { goal, correlationId -> emptyMap() }),

  STEP_STARTED("step-started", { goal, correlationId -> emptyMap() }),

  STEP_COMPLETED("step-completed", { goal, correlationId -> emptyMap() }),

  STEP_REMOVED(
    "step-removed",
    { goal, correlationId ->
      mapOf(
        "correlationId" to correlationId.toString(),
        "reference" to goal.reference.toString(),
        "stepCount" to goal.steps.size.toString(),
      )
    },
  ),

  STEP_ADDED("step-added", { goal, correlationId -> emptyMap() }),
}
