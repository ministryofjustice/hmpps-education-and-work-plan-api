package uk.gov.justice.digital.hmpps.domain.induction.dto

import uk.gov.justice.digital.hmpps.domain.induction.HighestEducationLevel
import uk.gov.justice.digital.hmpps.domain.induction.Qualification

data class CreatePreviousQualificationsDto(
  val educationLevel: HighestEducationLevel,
  val qualifications: List<Qualification>,
  val prisonId: String,
)
