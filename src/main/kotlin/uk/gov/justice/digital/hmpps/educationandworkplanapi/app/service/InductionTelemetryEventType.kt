package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Induction

/**
 * An enumeration of the types of Induction events that can be sent to the Telemetry Service, where each item has:
 *   * a text field `value` that is the name of the customEvent recorded in App Insights.
 *   * a function `customDimensions` that returns the customDimensions that are recorded with the App Insights event.
 */
enum class InductionTelemetryEventType(
  val value: String,
  val customDimensions: (induction: Induction) -> Map<String, String>,
) {
  INDUCTION_CREATED(
    "INDUCTION_CREATED",
    { induction ->
      mapOf(
        "reference" to induction.reference.toString(),
        "prisonId" to induction.createdAtPrison,
        "userId" to induction.createdBy!!,
      )
    },
  ),

  INDUCTION_UPDATED(
    "INDUCTION_UPDATED",
    { induction ->
      mapOf(
        "reference" to induction.reference.toString(),
        "prisonId" to induction.lastUpdatedAtPrison,
        "userId" to induction.lastUpdatedBy!!,
      )
    },
  ),
}
