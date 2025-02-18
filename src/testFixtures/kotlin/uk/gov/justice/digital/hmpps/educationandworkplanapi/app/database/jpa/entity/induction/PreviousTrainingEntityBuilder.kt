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
  updatedAt: Instant? = null,
  updatedAtPrison: String = "BXI",
  updatedBy: String? = null,
) = PreviousTrainingEntity(
  reference = reference,
  trainingTypes = trainingTypes,
  trainingTypeOther = trainingTypeOther,
  createdAtPrison = createdAtPrison,
  updatedAtPrison = updatedAtPrison,
).apply {
  this.id = id
  this.createdAt = createdAt
  this.createdBy = createdBy
  this.updatedAt = updatedAt
  this.updatedBy = updatedBy
}

fun aValidPreviousTrainingEntityWithJpaFieldsPopulated(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  trainingTypes: MutableList<TrainingType> = mutableListOf(TrainingType.OTHER),
  trainingTypeOther: String = "Kotlin course",
  createdAt: Instant = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String = "asmith_gen",
  updatedAt: Instant = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String = "bjones_gen",
) = PreviousTrainingEntity(
  reference = reference,
  trainingTypes = trainingTypes,
  trainingTypeOther = trainingTypeOther,
  createdAtPrison = createdAtPrison,
  updatedAtPrison = updatedAtPrison,
).apply {
  this.id = id
  this.createdAt = createdAt
  this.createdBy = createdBy
  this.updatedAt = updatedAt
  this.updatedBy = updatedBy
}
