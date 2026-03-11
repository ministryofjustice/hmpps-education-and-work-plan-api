package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.assessment.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.assessment.EducationAssessmentStatus
import java.time.LocalDate

data class CreateEducationAssessmentEventDto(
  val prisonNumber: String,
  val statusChangeDate: LocalDate,
  val status: EducationAssessmentStatus,
  val source: String,
  val detailUrl: String?,
  val prisonId: String,
)
