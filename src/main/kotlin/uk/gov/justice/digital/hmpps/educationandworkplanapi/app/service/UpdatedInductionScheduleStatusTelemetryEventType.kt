package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.UpdatedInductionScheduleStatus

/**
 * An enumeration of the types of updated Induction Schedule status events that can be sent to the Telemetry Service, where each item has:
 *   * a text field `value` that is the name of the customEvent recorded in App Insights.
 *   * a function `customDimensions` that returns the customDimensions that are recorded with the App Insights event.
 */
enum class UpdatedInductionScheduleStatusTelemetryEventType(
  val value: String,
  val customDimensions: (updatedInductionScheduleStatus: UpdatedInductionScheduleStatus) -> Map<String, String>,
) {
  INDUCTION_SCHEDULE_STATUS_UPDATED(
    "INDUCTION_SCHEDULE_STATUS_UPDATED",
    { updatedInductionScheduleStatus ->
      mapOf(
        "reference" to updatedInductionScheduleStatus.reference.toString(),
        "userId" to updatedInductionScheduleStatus.updatedBy,
      )
    },
  ),
}
