package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import java.time.Instant
import java.util.UUID

fun aValidInPrisonInterestsEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  inPrisonWorkInterests: List<InPrisonWorkInterestEntity> = listOf(aValidInPrisonWorkInterestEntity()),
  inPrisonTrainingInterests: List<InPrisonTrainingInterestEntity> = listOf(aValidInPrisonTrainingInterestEntity()),
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
