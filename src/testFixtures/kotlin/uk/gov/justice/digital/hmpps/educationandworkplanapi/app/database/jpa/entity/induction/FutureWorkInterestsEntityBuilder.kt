package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import java.time.Instant
import java.util.UUID

fun aValidFutureWorkInterestsEntity(
  id: UUID? = null,
  reference: UUID? = UUID.randomUUID(),
  interests: List<WorkInterestEntity> = listOf(aValidWorkInterestEntity()),
  createdAt: Instant? = null,
  createdAtPrison: String = "BXI",
  createdBy: String? = null,
  createdByDisplayName: String? = null,
  updatedAt: Instant? = null,
  updatedAtPrison: String = "BXI",
  updatedBy: String? = null,
  updatedByDisplayName: String? = null,
) =
  FutureWorkInterestsEntity(
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

fun aValidFutureWorkInterestsEntityWithJpaFieldsPopulated(
  id: UUID? = UUID.randomUUID(),
  reference: UUID? = UUID.randomUUID(),
  interests: List<WorkInterestEntity> = listOf(aValidWorkInterestEntity()),
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
  updatedByDisplayName: String? = "Barry Jones",
) =
  FutureWorkInterestsEntity(
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

fun aValidWorkInterestEntity(
  reference: UUID = UUID.randomUUID(),
  workType: WorkInterestType = WorkInterestType.OTHER,
  workTypeOther: String? = "Any job I can get",
  role: String? = "Any role",
) =
  WorkInterestEntity(
    reference = reference,
    workType = workType,
    workTypeOther = workTypeOther,
    role = role,
  )
