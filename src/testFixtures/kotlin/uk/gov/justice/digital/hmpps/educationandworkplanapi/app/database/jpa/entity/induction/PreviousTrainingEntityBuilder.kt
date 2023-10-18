package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import java.util.UUID

fun aValidPreviousTrainingEntity(
  reference: UUID = UUID.randomUUID(),
  trainingTypes: List<TrainingType> = listOf(TrainingType.OTHER),
  trainingTypeOther: String = "Kotlin course",
  createdAtPrison: String = "BXI",
  updatedAtPrison: String = "BXI",
) =
  PreviousTrainingEntity(
    reference = reference,
    trainingTypes = trainingTypes,
    trainingTypeOther = trainingTypeOther,
    createdAtPrison = createdAtPrison,
    updatedAtPrison = updatedAtPrison,
  )
