package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.EducationLevel
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Qualification

data class CreatePreviousQualificationsDto(
  val prisonNumber: String,
  val educationLevel: EducationLevel,
  val qualifications: List<Qualification>,
  val prisonId: String,
)
