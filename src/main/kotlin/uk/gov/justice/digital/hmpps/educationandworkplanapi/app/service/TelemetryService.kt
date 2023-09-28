package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.trackEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.TelemetryEventType.GOAL_CREATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.TelemetryEventType.GOAL_UPDATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.TelemetryEventType.STEP_REMOVED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal

/**
 * Service class exposing methods to log telemetry events to ApplicationInsights.
 */
@Service
class TelemetryService(
  private val telemetryClient: TelemetryClient,
  private val telemetryEventTypeResolver: TelemetryEventTypeResolver,
) {

  fun trackGoalCreatedEvent(goal: Goal) {
    telemetryClient.trackEvent(GOAL_CREATED.value, createEventCustomDimensions(goal))
  }

  fun trackGoalUpdatedEvent(goal: Goal) {
    telemetryClient.trackEvent(GOAL_UPDATED.value, updateEventCustomDimensions(goal))
  }

  fun trackStepRemovedEvent(goal: Goal) {
    telemetryClient.trackEvent(STEP_REMOVED.value, stepRemoveEventCustomDimensions(goal))
  }

  /**
   * Sends all goal update telemetry tracking events based on the differences between the previousGoal and the
   * updatedGoal.
   */
  fun trackGoalUpdatedEvents(previousGoal: Goal, updatedGoal: Goal) {
    val telemetryUpdateEvents =
      telemetryEventTypeResolver.resolveUpdateEventTypes(previousGoal = previousGoal, updatedGoal = updatedGoal)
    telemetryUpdateEvents.forEach {
      when (it) {
        GOAL_UPDATED -> trackGoalUpdatedEvent(updatedGoal)
        STEP_REMOVED -> trackStepRemovedEvent(updatedGoal)
        else -> {}
      }
    }
  }

  /**
   * Returns a map of data representing the custom dimensions for the `goal-create` event.
   */
  private fun createEventCustomDimensions(goal: Goal): Map<String, String> =
    with(goal) {
      mapOf(
        "status" to status.name,
        "stepCount" to steps.size.toString(),
        "reference" to reference.toString(),
        "notesCharacterCount" to (notes?.length ?: 0).toString(),
      )
    }

  /**
   * Returns a map of data representing the custom dimensions for the `goal-update` event.
   */
  private fun updateEventCustomDimensions(goal: Goal): Map<String, String> =
    with(goal) {
      mapOf(
        "reference" to reference.toString(),
        "notesCharacterCount" to (notes?.length ?: 0).toString(),
      )
    }

  /**
   * Returns a map of data representing the custom dimensions for the `step-remove` event.
   */
  private fun stepRemoveEventCustomDimensions(goal: Goal): Map<String, String> =
    with(goal) {
      mapOf(
        "reference" to reference.toString(),
        "stepCount" to steps.size.toString(),
      )
    }
}
