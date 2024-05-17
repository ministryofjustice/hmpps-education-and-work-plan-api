package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.TrainingType

data class CreatePreviousTrainingDto(
  val trainingTypes: List<TrainingType>,
  val trainingTypeOther: String?,
  val prisonId: String,
)
