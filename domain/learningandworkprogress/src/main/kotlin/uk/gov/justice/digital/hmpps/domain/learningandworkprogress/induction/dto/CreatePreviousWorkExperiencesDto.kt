package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HasWorkedBefore
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkExperience

data class CreatePreviousWorkExperiencesDto(
  val hasWorkedBefore: HasWorkedBefore,
  val experiences: List<WorkExperience>,
  val prisonId: String,
)
