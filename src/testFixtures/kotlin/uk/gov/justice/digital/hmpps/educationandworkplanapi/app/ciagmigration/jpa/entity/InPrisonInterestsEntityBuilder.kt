package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity

import java.time.Instant
import java.util.UUID

fun aValidInPrisonInterestsMigrationEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  inPrisonWorkInterests: MutableList<InPrisonWorkInterestMigrationEntity> = mutableListOf(
    aValidInPrisonWorkInterestMigrationEntity(),
  ),
  inPrisonTrainingInterests: MutableList<InPrisonTrainingInterestMigrationEntity> = mutableListOf(
    aValidInPrisonTrainingInterestMigrationEntity(),
  ),
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
  updatedByDisplayName: String? = "Barry Jones",
) =
  InPrisonInterestsMigrationEntity(
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

fun aValidInPrisonWorkInterestMigrationEntity(
  reference: UUID = UUID.randomUUID(),
  workType: InPrisonWorkType = InPrisonWorkType.OTHER,
  workTypeOther: String? = "Any in-prison work",
  createdAt: Instant? = Instant.now(),
  createdBy: String? = "asmith_gen",
  updatedAt: Instant? = Instant.now(),
  updatedBy: String? = "bjones_gen",
) =
  InPrisonWorkInterestMigrationEntity(
    reference = reference,
    workType = workType,
    workTypeOther = workTypeOther,
    createdAt = createdAt,
    createdBy = createdBy,
    updatedAt = updatedAt,
    updatedBy = updatedBy,
  )

fun aValidInPrisonTrainingInterestMigrationEntity(
  reference: UUID = UUID.randomUUID(),
  trainingType: InPrisonTrainingType = InPrisonTrainingType.OTHER,
  trainingTypeOther: String? = "Any in-prison training",
  createdAt: Instant? = Instant.now(),
  createdBy: String? = "asmith_gen",
  updatedAt: Instant? = Instant.now(),
  updatedBy: String? = "bjones_gen",
) =
  InPrisonTrainingInterestMigrationEntity(
    reference = reference,
    trainingType = trainingType,
    trainingTypeOther = trainingTypeOther,
    createdAt = createdAt,
    createdBy = createdBy,
    updatedAt = updatedAt,
    updatedBy = updatedBy,
  )
