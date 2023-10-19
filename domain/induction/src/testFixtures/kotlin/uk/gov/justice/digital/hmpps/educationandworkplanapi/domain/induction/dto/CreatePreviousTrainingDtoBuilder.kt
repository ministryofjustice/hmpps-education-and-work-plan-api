package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.TrainingType

fun aValidCreatePreviousTrainingDto(
  trainingTypes: List<TrainingType> = listOf(TrainingType.OTHER),
  trainingTypeOther: String? = "Kotlin course",
  prisonId: String = "BXI",
) =
  CreatePreviousTrainingDto(
    trainingTypes = trainingTypes,
    trainingTypeOther = trainingTypeOther,
    prisonId = prisonId,
  )
