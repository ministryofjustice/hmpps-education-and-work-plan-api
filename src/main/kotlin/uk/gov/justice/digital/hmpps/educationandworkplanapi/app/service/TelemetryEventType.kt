package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

/**
 * An enumeration of the types of Goal events that can be sent to the Telemetry Service.
 */
enum class TelemetryEventType(val value: String) {
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
