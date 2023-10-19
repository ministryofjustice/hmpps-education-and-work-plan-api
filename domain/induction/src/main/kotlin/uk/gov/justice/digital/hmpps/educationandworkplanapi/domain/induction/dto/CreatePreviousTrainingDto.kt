package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.TrainingType

data class CreatePreviousTrainingDto(
  val trainingTypes: List<TrainingType>,
  val trainingTypeOther: String?,
  val prisonId: String,
)
