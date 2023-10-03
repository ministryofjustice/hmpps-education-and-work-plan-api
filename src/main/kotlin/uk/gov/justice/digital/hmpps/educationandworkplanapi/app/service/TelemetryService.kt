package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.trackEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.TelemetryEventType.GOAL_CREATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.TelemetryEventType.GOAL_UPDATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.TelemetryEventType.STEP_REMOVED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import java.util.UUID

/**
 * Service class exposing methods to log telemetry events to ApplicationInsights.
 */
@Service
class TelemetryService(
  private val telemetryClient: TelemetryClient,
  private val telemetryEventTypeResolver: TelemetryEventTypeResolver,
) {

  fun trackGoalCreatedEvent(createdGoal: Goal, correlationId: UUID = UUID.randomUUID()) {
    sendTelemetryEventForGoal(
      goal = createdGoal,
      correlationId = correlationId,
      telemetryEventType = GOAL_CREATED,
    )
  }

  fun trackGoalUpdatedEvent(updatedGoal: Goal, correlationId: UUID = UUID.randomUUID()) {
    sendTelemetryEventForGoal(
      goal = updatedGoal,
      correlationId = correlationId,
      telemetryEventType = GOAL_UPDATED,
    )
  }

  fun trackStepRemovedEvent(updatedGoal: Goal, correlationId: UUID = UUID.randomUUID()) {
    sendTelemetryEventForGoal(
      goal = updatedGoal,
      correlationId = correlationId,
      telemetryEventType = STEP_REMOVED,
    )
  }

  /**
   * Sends all goal update telemetry tracking events based on the differences between the previousGoal and the
   * updatedGoal.
   */
  fun trackGoalUpdatedEvents(previousGoal: Goal, updatedGoal: Goal) {
    val correlationId = UUID.randomUUID()
    val telemetryUpdateEvents =
      telemetryEventTypeResolver.resolveUpdateEventTypes(previousGoal = previousGoal, updatedGoal = updatedGoal)
    telemetryUpdateEvents.forEach {
      when (it) {
        GOAL_UPDATED -> trackGoalUpdatedEvent(updatedGoal, correlationId)
        STEP_REMOVED -> trackStepRemovedEvent(updatedGoal, correlationId)
        else -> {}
      }
    }
  }

  private fun sendTelemetryEventForGoal(goal: Goal, correlationId: UUID, telemetryEventType: TelemetryEventType) =
    telemetryClient.trackEvent(telemetryEventType.value, telemetryEventType.customDimensions(goal, correlationId))
}
