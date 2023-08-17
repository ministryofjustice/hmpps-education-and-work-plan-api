package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import com.microsoft.applicationinsights.TelemetryClient
import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal

private val log = KotlinLogging.logger {}

/**
 * Service class exposing methods to log telemetry events to ApplicationInsights.
 */
@Service
class TelemetryService(
  /*
  The TelemetryClient is instantiated and injected at runtime by ApplicationInsights java agent via AppInsightsConfigManager
  in the dps-gradle-spring-boot-plugin (https://github.com/ministryofjustice/dps-gradle-spring-boot/blob/spring-boot-3/src/main/kotlin/uk/gov/justice/digital/hmpps/gradle/configmanagers/AppInsightsConfigManager.kt#L35)
  where the jar is available at runtime courtesy of the Dockerfile. This means that in reality there will only be a TelemetryClient
  bean in a deployed environment (ie. a deployed docker container).
  In non-docker runtimes (eg: running in the IDE, or running the integration tests), this bean will not exist; hence the
  nullable field and IDE warning.
   */
  private val telemetryClient: TelemetryClient?,
) {

  init {
    if (telemetryClient == null) {
      log.warn { "No TelemetryClient injected at runtime. No telemetry events will be published." }
    }
  }

  companion object {
    private const val GOAL_CREATE_EVENT = "goal-create"
  }

  fun trackGoalCreateEvent(goal: Goal) {
    telemetryClient?.trackEvent(GOAL_CREATE_EVENT, goal.goalCreateEventCustomDimensions())
  }

  private fun Goal.goalCreateEventCustomDimensions(): Map<String, String> =
    mapOf(
      "status" to this.status.name,
      "stepCount" to this.steps.size.toString(),
      "reference" to this.reference.toString(),
    )
}

fun TelemetryClient.trackEvent(name: String, properties: Map<String, String>) =
  this.trackEvent(name, properties, null)
