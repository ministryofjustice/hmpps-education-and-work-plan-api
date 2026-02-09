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
    { inductionSchedule ->
      mapOf(
        "reference" to inductionSchedule.reference.toString(),
        "userId" to inductionSchedule.lastUpdatedBy,
        "deadlineDate" to inductionSchedule.deadlineDate.toString(),
        "scheduleStatus" to inductionSchedule.scheduleStatus.name,
        "scheduleCalculationRule" to inductionSchedule.scheduleCalculationRule.name,
      )
    },
  ),
}
