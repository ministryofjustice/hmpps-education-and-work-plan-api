package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import java.time.LocalDate

data class AssessmentEventDto(
  val prisonNumber: String,
  val status: AssessmentEventStatus,
  val statusChangeDate: LocalDate,
  val detailUrl: String?,
)

enum class AssessmentEventStatus {
  ALL_RELEVANT_ASSESSMENTS_COMPLETE,
}
