package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.TrainingType
import java.util.UUID

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

fun aValidUpdatePreviousTrainingDto(
  reference: UUID = UUID.randomUUID(),
  trainingTypes: List<TrainingType> = listOf(TrainingType.OTHER),
  trainingTypeOther: String? = "Kotlin course",
  prisonId: String = "BXI",
) =
  UpdatePreviousTrainingDto(
    reference = reference,
    trainingTypes = trainingTypes,
    trainingTypeOther = trainingTypeOther,
    prisonId = prisonId,
  )
