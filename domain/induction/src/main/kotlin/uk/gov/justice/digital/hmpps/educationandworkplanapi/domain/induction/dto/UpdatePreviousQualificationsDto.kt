package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.HighestEducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Qualification
import java.util.UUID

data class UpdatePreviousQualificationsDto(
  val reference: UUID,
  val educationLevel: HighestEducationLevel,
  val qualifications: List<Qualification>,
  val prisonId: String,
)
