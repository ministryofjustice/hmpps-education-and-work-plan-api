package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule

/**
 * An enumeration of the types of Induction events that can be sent to the Telemetry Service, where each item has:
 *   * a text field `value` that is the name of the customEvent recorded in App Insights.
 *   * a function `customDimensions` that returns the customDimensions that are recorded with the App Insights event.
 */
enum class InductionScheduleTelemetryEventType(
  val value: String,
  val customDimensions: (inductionSchedule: InductionSchedule) -> Map<String, String>,
) {
  INDUCTION_SCHEDULE_CREATED(
    "INDUCTION_SCHEDULE_CREATED",
    { induction ->
      mapOf(
        "reference" to induction.reference.toString(),
        "userId" to induction.createdBy!!,
        "deadlineDate" to induction.deadlineDate.toString(),
        "scheduleStatus" to induction.scheduleStatus.name,
        "scheduleCalculationRule" to induction.scheduleCalculationRule.name,
      )
    },
  ),

  INDUCTION_SCHEDULE_UPDATED(
    "INDUCTION_SCHEDULE_UPDATED",
    { induction ->
      mapOf(
        "reference" to induction.reference.toString(),
        "userId" to induction.createdBy!!,
        "deadlineDate" to induction.deadlineDate.toString(),
        "scheduleStatus" to induction.scheduleStatus.name,
        "scheduleCalculationRule" to induction.scheduleCalculationRule.name,
      )
    },
  ),
}
