package uk.gov.justice.digital.hmpps.educationandworkplanapi.ciagmigration.entity

import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.induction.FutureWorkInterestsMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.induction.WorkInterestMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.induction.WorkInterestType
import java.time.Instant
import java.util.UUID

fun aValidFutureWorkInterestsMigrationEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID? = UUID.randomUUID(),
  interests: MutableList<WorkInterestMigrationEntity> = mutableListOf(aValidWorkInterestMigrationEntity()),
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
  updatedByDisplayName: String? = "Barry Jones",
) =
  FutureWorkInterestsMigrationEntity(
    id = id,
    reference = reference,
    interests = interests,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    updatedAt = updatedAt,
    updatedAtPrison = updatedAtPrison,
    updatedBy = updatedBy,
    updatedByDisplayName = updatedByDisplayName,
  )

fun aValidWorkInterestMigrationEntity(
  reference: UUID = UUID.randomUUID(),
  workType: WorkInterestType = WorkInterestType.OTHER,
  workTypeOther: String? = "Any job I can get",
  role: String? = "Any role",
) =
  WorkInterestMigrationEntity(
    reference = reference,
    workType = workType,
    workTypeOther = workTypeOther,
    role = role,
  )
