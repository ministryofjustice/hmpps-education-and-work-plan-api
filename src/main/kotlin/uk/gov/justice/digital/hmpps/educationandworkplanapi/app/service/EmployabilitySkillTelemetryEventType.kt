package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import uk.gov.justice.digital.hmpps.domain.personallearningplan.EmployabilitySkill

/**
 * An enumeration of the types of Induction events that can be sent to the Telemetry Service, where each item has:
 *   * a text field `value` that is the name of the customEvent recorded in App Insights.
 *   * a function `customDimensions` that returns the customDimensions that are recorded with the App Insights event.
 */
enum class EmployabilitySkillTelemetryEventType(
  val value: String,
  val customDimensions: (employabilitySkill: EmployabilitySkill) -> Map<String, String>,
) {
  EMPLOYABILITY_SKILL_CREATED(
    "EMPLOYABILITY_SKILL_CREATED",
    { induction ->
      mapOf(
        "reference" to induction.reference.toString(),
        "prisonId" to induction.createdAtPrison,
        "userId" to induction.createdBy,
      )
    },
  ),
}
