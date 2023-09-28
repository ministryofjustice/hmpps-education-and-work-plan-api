package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

/**
 * A granular enumeration of the type of Goal update events that can be sent to the Telemetry Service.
 */
enum class TelemetryUpdateEventType(val value: String) {
  GOAL_CREATED("goal-created"),
  GOAL_UPDATED("goal-updated"),
  GOAL_STARTED("goal-started"),
  GOAL_COMPLETED("goal-completed"),
  GOAL_ARCHIVED("goal-archived"),
  STEP_UPDATED("step-updated"),
  STEP_NOT_STARTED("step-not-started"),
  STEP_STARTED("step-started"),
  STEP_COMPLETED("step-completed"),
  STEP_REMOVED("step-removed"),
  STEP_ADDED("step-added"),
}
