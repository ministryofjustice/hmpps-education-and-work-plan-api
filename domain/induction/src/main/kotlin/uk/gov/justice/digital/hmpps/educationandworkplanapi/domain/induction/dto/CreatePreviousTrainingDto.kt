package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.TrainingType

data class CreatePreviousTrainingDto(
  val trainingType: List<TrainingType>,
  val trainingTypeOther: String?,
  val prisonId: String,
)
