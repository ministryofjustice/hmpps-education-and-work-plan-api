package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import java.time.Instant
import java.util.UUID

fun aValidPreviousTrainingEntity(
  id: UUID? = null,
  reference: UUID = UUID.randomUUID(),
  trainingTypes: MutableList<TrainingType> = mutableListOf(TrainingType.OTHER),
  trainingTypeOther: String? = "Kotlin course",
  createdAt: Instant? = null,
  createdAtPrison: String = "BXI",
  createdBy: String? = null,
  createdByDisplayName: String? = null,
  updatedAt: Instant? = null,
  updatedAtPrison: String = "BXI",
  updatedBy: String? = null,
  updatedByDisplayName: String? = null,
) =
  PreviousTrainingEntity(
    id = id,
    reference = reference,
    trainingTypes = trainingTypes,
    trainingTypeOther = trainingTypeOther,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    updatedAt = updatedAt,
    updatedAtPrison = updatedAtPrison,
    updatedBy = updatedBy,
    updatedByDisplayName = updatedByDisplayName,
  )

fun aValidPreviousTrainingEntityWithJpaFieldsPopulated(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  trainingTypes: MutableList<TrainingType> = mutableListOf(TrainingType.OTHER),
  trainingTypeOther: String = "Kotlin course",
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
  updatedByDisplayName: String? = "Barry Jones",
) =
  PreviousTrainingEntity(
    id = id,
    reference = reference,
    trainingTypes = trainingTypes,
    trainingTypeOther = trainingTypeOther,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    updatedAt = updatedAt,
    updatedAtPrison = updatedAtPrison,
    updatedBy = updatedBy,
    updatedByDisplayName = updatedByDisplayName,
  )
