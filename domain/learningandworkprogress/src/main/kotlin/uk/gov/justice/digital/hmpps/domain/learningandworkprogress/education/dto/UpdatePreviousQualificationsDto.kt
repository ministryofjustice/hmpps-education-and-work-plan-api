package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationLevel
import java.util.UUID

data class UpdatePreviousQualificationsDto(
  val reference: UUID?,
  val educationLevel: EducationLevel?,
  val qualifications: List<UpdateOrCreateQualificationDto>,
  val prisonId: String,
)
