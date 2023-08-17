package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import com.microsoft.applicationinsights.TelemetryClient
import io.opentelemetry.api.baggage.Baggage
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.trackEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal

/**
 * Service class exposing methods to log telemetry events to ApplicationInsights.
 */
@Service
class TelemetryService(
  private val telemetryClient: TelemetryClient,
) {

  companion object {
    private const val GOAL_CREATE_EVENT = "goal-create"
  }

  fun trackGoalCreateEvent(goal: Goal) {
    telemetryClient.trackEvent(
      GOAL_CREATE_EVENT,
      getExistingCustomDimensions().plus(createEventCustomDimensions(goal)),
    )
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
      )
    }

  /**
   * Returns a map of data representing any custom dimensions already set via the opentelemetry API.
   */
  private fun getExistingCustomDimensions(): Map<String, String> =
    Baggage.current().asMap().mapValues {
      val baggageEntry = it.value
      baggageEntry.value
    }
}
