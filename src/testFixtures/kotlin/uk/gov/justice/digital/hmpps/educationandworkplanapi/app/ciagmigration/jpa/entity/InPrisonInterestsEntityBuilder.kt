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
  workTypeOther: String? = "Any work type",
) =
  InPrisonWorkInterestMigrationEntity(
    reference = reference,
    workType = workType,
    workTypeOther = workTypeOther,
  )

fun aValidInPrisonTrainingInterestMigrationEntity(
  reference: UUID = UUID.randomUUID(),
  trainingType: InPrisonTrainingType = InPrisonTrainingType.OTHER,
  trainingTypeOther: String? = "Any training type",
) =
  InPrisonTrainingInterestMigrationEntity(
    reference = reference,
    trainingType = trainingType,
    trainingTypeOther = trainingTypeOther,
  )
