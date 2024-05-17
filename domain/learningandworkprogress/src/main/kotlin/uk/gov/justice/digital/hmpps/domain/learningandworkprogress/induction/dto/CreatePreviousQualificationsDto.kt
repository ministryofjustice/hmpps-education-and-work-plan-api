package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HighestEducationLevel
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Qualification

data class CreatePreviousQualificationsDto(
  val educationLevel: HighestEducationLevel,
  val qualifications: List<Qualification>,
  val prisonId: String,
)
