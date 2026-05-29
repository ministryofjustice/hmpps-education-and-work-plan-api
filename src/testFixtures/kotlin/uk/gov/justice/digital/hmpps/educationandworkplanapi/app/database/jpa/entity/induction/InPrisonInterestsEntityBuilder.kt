package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import java.time.Instant
import java.util.UUID

fun aValidInPrisonInterestsEntity(
  id: UUID? = null,
  reference: UUID = UUID.randomUUID(),
  inPrisonWorkInterests: MutableList<InPrisonWorkInterestEntity> = mutableListOf(aValidInPrisonWorkInterestEntity()),
  inPrisonTrainingInterests: MutableList<InPrisonTrainingInterestEntity> = mutableListOf(aValidInPrisonTrainingInterestEntity()),
  createdAt: Instant? = null,
  createdAtPrison: String = "BXI",
  createdBy: String? = null,
  updatedAt: Instant? = null,
  updatedAtPrison: String = "BXI",
  updatedBy: String? = null,
) = InPrisonInterestsEntity(
  reference = reference,
  inPrisonWorkInterests = inPrisonWorkInterests,
  inPrisonTrainingInterests = inPrisonTrainingInterests,
  createdAtPrison = createdAtPrison,
  updatedAtPrison = updatedAtPrison,
).apply {
  this.id = id
  this.createdAt = createdAt
  this.createdBy = createdBy
  this.updatedAt = updatedAt
  this.updatedBy = updatedBy
}

fun aValidInPrisonInterestsEntityWithJpaFieldsPopulated(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  inPrisonWorkInterests: MutableList<InPrisonWorkInterestEntity> = mutableListOf(aValidInPrisonWorkInterestEntity()),
  inPrisonTrainingInterests: MutableList<InPrisonTrainingInterestEntity> = mutableListOf(aValidInPrisonTrainingInterestEntity()),
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String? = "asmith_gen",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
) = InPrisonInterestsEntity(
  reference = reference,
  inPrisonWorkInterests = inPrisonWorkInterests,
  inPrisonTrainingInterests = inPrisonTrainingInterests,
  createdAtPrison = createdAtPrison,
  updatedAtPrison = updatedAtPrison,
).apply {
  this.id = id
  this.createdAt = createdAt
  this.createdBy = createdBy
  this.updatedAt = updatedAt
  this.updatedBy = updatedBy
}

fun aValidInPrisonWorkInterestEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  workType: InPrisonWorkType = InPrisonWorkType.OTHER,
  workTypeOther: String? = "Any work type",
  createdAt: Instant? = Instant.now(),
  createdBy: String? = "asmith_gen",
  updatedAt: Instant? = Instant.now(),
  updatedBy: String? = "bjones_gen",
) = InPrisonWorkInterestEntity(
  reference = reference,
  workType = workType,
  workTypeOther = workTypeOther,
).apply {
  this.id = id
  this.createdAt = createdAt
  this.createdBy = createdBy
  this.updatedAt = updatedAt
  this.updatedBy = updatedBy
}

fun aValidInPrisonTrainingInterestEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  trainingType: InPrisonTrainingType = InPrisonTrainingType.OTHER,
  trainingTypeOther: String? = "Any training type",
  createdAt: Instant? = Instant.now(),
  createdBy: String? = "asmith_gen",
  updatedAt: Instant? = Instant.now(),
  updatedBy: String? = "bjones_gen",
) = InPrisonTrainingInterestEntity(
  reference = reference,
  trainingType = trainingType,
  trainingTypeOther = trainingTypeOther,
).apply {
  this.id = id
  this.createdAt = createdAt
  this.createdBy = createdBy
  this.updatedAt = updatedAt
  this.updatedBy = updatedBy
}
