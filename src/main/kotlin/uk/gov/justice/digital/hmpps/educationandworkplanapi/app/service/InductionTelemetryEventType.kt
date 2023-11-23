package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Induction
import java.util.UUID

/**
 * An enumeration of the types of Induction events that can be sent to the Telemetry Service, where each item has:
 *   * a text field `value` that is the name of the customEvent recorded in App Insights.
 *   * a function `customDimensions` that returns the customDimensions that are recorded with the App Insights event.
 */
enum class InductionTelemetryEventType(
  val value: String,
  val customDimensions: (induction: Induction, correlationId: UUID) -> Map<String, String>,
) {
  INDUCTION_CREATED(
    "induction-created",
    { induction, correlationId ->
      mapOf(
        "correlationId" to correlationId.toString(),
        "reference" to induction.reference.toString(),
        "prisonId" to induction.createdAtPrison,
        "userId" to induction.createdBy!!,
        "timestamp" to induction.createdAt.toString(),
      )
    },
  ),

  INDUCTION_UPDATED(
    "induction-updated",
    { induction, correlationId ->
      mapOf(
        "correlationId" to correlationId.toString(),
        "reference" to induction.reference.toString(),
        "prisonId" to induction.lastUpdatedAtPrison,
        "userId" to induction.lastUpdatedBy!!,
        "timestamp" to induction.lastUpdatedAt.toString(),
      )
    },
  ),
}
