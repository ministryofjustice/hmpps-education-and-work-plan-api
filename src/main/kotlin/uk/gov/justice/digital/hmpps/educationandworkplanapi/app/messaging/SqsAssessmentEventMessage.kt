package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import java.time.LocalDate

data class SqsAssessmentEventMessage(
  val prisonNumber: String,
  val status: EducationAssessmentStatus,
  val statusChangeDate: LocalDate,
  val detailUrl: String?,
  val requestId: String,
)

enum class EducationAssessmentStatus {
  ALL_RELEVANT_ASSESSMENTS_COMPLETE,
}
