package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkExperience

data class CreatePreviousWorkExperiencesDto(
  val hasWorkedBefore: Boolean,
  val experiences: List<WorkExperience>,
  val prisonId: String,
)
