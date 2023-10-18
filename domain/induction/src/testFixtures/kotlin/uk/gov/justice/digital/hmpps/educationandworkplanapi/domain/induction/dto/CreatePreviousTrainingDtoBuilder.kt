package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.TrainingType

fun aValidCreatePreviousTrainingDto(
  trainingType: List<TrainingType> = listOf(TrainingType.CSCS_CARD),
  trainingTypeOther: String? = null,
  prisonId: String = "BXI",
) =
  CreatePreviousTrainingDto(
    trainingType = trainingType,
    trainingTypeOther = trainingTypeOther,
    prisonId = prisonId,
  )
