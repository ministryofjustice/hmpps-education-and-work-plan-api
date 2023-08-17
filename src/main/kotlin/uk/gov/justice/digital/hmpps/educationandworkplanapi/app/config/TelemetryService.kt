package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Service
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
    telemetryClient.trackEvent(GOAL_CREATE_EVENT, goal.goalCreateEventCustomDimensions())
  }

  private fun Goal.goalCreateEventCustomDimensions(): Map<String, String> =
    mapOf(
      "status" to this.status.name,
      "stepCount" to this.steps.size.toString(),
      "reference" to this.reference.toString(),
    )
}
