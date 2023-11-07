package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkExperience
import java.util.UUID

data class UpdatePreviousWorkExperiencesDto(
  val reference: UUID,
  val experiences: List<WorkExperience>?,
  val prisonId: String,
)
