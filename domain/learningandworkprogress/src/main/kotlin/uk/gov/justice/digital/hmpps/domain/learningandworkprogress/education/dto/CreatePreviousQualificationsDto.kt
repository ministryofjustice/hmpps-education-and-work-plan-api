package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationLevel
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.Qualification

data class CreatePreviousQualificationsDto(
  val prisonNumber: String,
  val educationLevel: EducationLevel,
  val qualifications: List<Qualification>,
  val prisonId: String,
)
