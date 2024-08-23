package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationLevel

data class CreatePreviousQualificationsDto(
  val prisonNumber: String,
  val educationLevel: EducationLevel,
  val qualifications: List<UpdateOrCreateQualificationDto>,
  val prisonId: String,
)
