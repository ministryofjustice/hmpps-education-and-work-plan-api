package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventEntity

enum class EducationAssessmentEventTelemetryEventType(
  val value: String,
  val customDimensions: (entity: EducationAssessmentEventEntity) -> Map<String, String>,
) {
  EDUCATION_ASSESSMENT_EVENT_CREATED(
    "EDUCATION_ASSESSMENT_EVENT_CREATED",
    { entity ->
      mapOf(
        "reference" to entity.reference.toString(),
        "prisonNumber" to entity.prisonNumber,
        "statusChangeDate" to entity.statusChangeDate.toString(),
        "status" to entity.status.name,
        "source" to entity.source,
      )
    },
  ),
}
