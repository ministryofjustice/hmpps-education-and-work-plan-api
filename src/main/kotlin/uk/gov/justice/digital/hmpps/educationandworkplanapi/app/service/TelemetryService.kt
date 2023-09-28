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
    sendTelemetryEventForGoal(goal, GOAL_CREATED)
  }

  fun trackGoalUpdatedEvent(goal: Goal) {
    sendTelemetryEventForGoal(goal, GOAL_UPDATED)
  }

  fun trackStepRemovedEvent(goal: Goal) {
    sendTelemetryEventForGoal(goal, STEP_REMOVED)
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

  private fun sendTelemetryEventForGoal(goal: Goal, telemetryEventType: TelemetryEventType) =
    telemetryClient.trackEvent(telemetryEventType.value, telemetryEventType.customDimensions(goal))
}
