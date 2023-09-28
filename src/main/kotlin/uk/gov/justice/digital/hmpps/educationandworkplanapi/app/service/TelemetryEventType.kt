package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal

/**
 * An enumeration of the types of Goal events that can be sent to the Telemetry Service, where each item has:
 *   * a text field `value` that is the name of the customEvent recorded in App Insights.
 *   * a function `customDimensions` that returns the customDimensions that are recorded with the App Insights event.
 */
enum class TelemetryEventType(val value: String, val customDimensions: (goal: Goal) -> Map<String, String>) {
  GOAL_CREATED(
    "goal-created",
    {
      mapOf(
        "status" to it.status.name,
        "stepCount" to it.steps.size.toString(),
        "reference" to it.reference.toString(),
        "notesCharacterCount" to (it.notes?.length ?: 0).toString(),
      )
    },
  ),

  GOAL_UPDATED(
    "goal-updated",
    {
      mapOf(
        "reference" to it.reference.toString(),
        "notesCharacterCount" to (it.notes?.length ?: 0).toString(),
      )
    },
  ),

  GOAL_STARTED("goal-started", { emptyMap() }),

  GOAL_COMPLETED("goal-completed", { emptyMap() }),

  GOAL_ARCHIVED("goal-archived", { emptyMap() }),

  STEP_UPDATED("step-updated", { emptyMap() }),

  STEP_NOT_STARTED("step-not-started", { emptyMap() }),

  STEP_STARTED("step-started", { emptyMap() }),

  STEP_COMPLETED("step-completed", { emptyMap() }),

  STEP_REMOVED(
    "step-removed",
    {
      mapOf(
        "reference" to it.reference.toString(),
        "stepCount" to it.steps.size.toString(),
      )
    },
  ),

  STEP_ADDED("step-added", { emptyMap() }),
}
