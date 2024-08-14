package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationLevel
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.Qualification
import java.util.UUID

data class UpdatePreviousQualificationsDto(
  val reference: UUID?,
  val educationLevel: EducationLevel?,
  val qualifications: List<Qualification>,
  val prisonId: String,
)
