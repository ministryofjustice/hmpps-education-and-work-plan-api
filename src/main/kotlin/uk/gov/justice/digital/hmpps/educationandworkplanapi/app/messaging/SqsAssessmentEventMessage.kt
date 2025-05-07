package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import java.time.LocalDate

data class SqsAssessmentEventMessage(
  val messageId: String,
  val eventType: String,
  val description: String? = "",
  val messageAttributes: MessageAttributes,
  val who: String? = "",
)

data class MessageAttributes(
  val prisonNumber: String,
  val status: EducationAssessmentStatus,
  val statusChangeDate: LocalDate, // could use LocalDate if needed
  val detailUrl: String?,
  val requestId: String,
)

enum class EducationAssessmentStatus {
  ALL_RELEVANT_ASSESSMENTS_COMPLETE,
}
