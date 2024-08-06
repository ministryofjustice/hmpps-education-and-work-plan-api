package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.EducationLevel
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Qualification
import java.util.UUID

data class UpdatePreviousQualificationsDto(
  val reference: UUID?,
  val educationLevel: EducationLevel?,
  val qualifications: List<Qualification>,
  val prisonId: String,
)
