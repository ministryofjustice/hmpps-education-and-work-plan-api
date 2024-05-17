package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkExperience
import java.util.UUID

data class UpdatePreviousWorkExperiencesDto(
  val reference: UUID?,
  val hasWorkedBefore: Boolean,
  val experiences: List<WorkExperience>,
  val prisonId: String,
)
