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
  createdByDisplayName: String? = null,
  updatedAt: Instant? = null,
  updatedAtPrison: String = "BXI",
  updatedBy: String? = null,
  updatedByDisplayName: String? = null,
) =
  InPrisonInterestsEntity(
    id = id,
    reference = reference,
    inPrisonWorkInterests = inPrisonWorkInterests,
    inPrisonTrainingInterests = inPrisonTrainingInterests,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    updatedAt = updatedAt,
    updatedAtPrison = updatedAtPrison,
    updatedBy = updatedBy,
    updatedByDisplayName = updatedByDisplayName,
  )

fun aValidInPrisonInterestsEntityWithJpaFieldsPopulated(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  inPrisonWorkInterests: MutableList<InPrisonWorkInterestEntity> = mutableListOf(aValidInPrisonWorkInterestEntity()),
  inPrisonTrainingInterests: MutableList<InPrisonTrainingInterestEntity> = mutableListOf(aValidInPrisonTrainingInterestEntity()),
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
  updatedByDisplayName: String? = "Barry Jones",
) =
  InPrisonInterestsEntity(
    id = id,
    reference = reference,
    inPrisonWorkInterests = inPrisonWorkInterests,
    inPrisonTrainingInterests = inPrisonTrainingInterests,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    updatedAt = updatedAt,
    updatedAtPrison = updatedAtPrison,
    updatedBy = updatedBy,
    updatedByDisplayName = updatedByDisplayName,
  )

fun aValidInPrisonWorkInterestEntity(
  reference: UUID = UUID.randomUUID(),
  workType: InPrisonWorkType = InPrisonWorkType.OTHER,
  workTypeOther: String? = "Any work type",
) =
  InPrisonWorkInterestEntity(
    reference = reference,
    workType = workType,
    workTypeOther = workTypeOther,
  )

fun aValidInPrisonTrainingInterestEntity(
  reference: UUID = UUID.randomUUID(),
  trainingType: InPrisonTrainingType = InPrisonTrainingType.OTHER,
  trainingTypeOther: String? = "Any training type",
) =
  InPrisonTrainingInterestEntity(
    reference = reference,
    trainingType = trainingType,
    trainingTypeOther = trainingTypeOther,
  )
