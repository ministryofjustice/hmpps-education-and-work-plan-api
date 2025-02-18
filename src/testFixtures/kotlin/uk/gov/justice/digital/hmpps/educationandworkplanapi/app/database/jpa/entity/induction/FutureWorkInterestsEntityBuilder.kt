package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import java.time.Instant
import java.util.UUID

fun aValidFutureWorkInterestsEntity(
  id: UUID? = null,
  reference: UUID = UUID.randomUUID(),
  interests: MutableList<WorkInterestEntity> = mutableListOf(aValidWorkInterestEntity()),
  createdAt: Instant? = null,
  createdAtPrison: String = "BXI",
  createdBy: String? = null,
  updatedAt: Instant? = null,
  updatedAtPrison: String = "BXI",
  updatedBy: String? = null,
) = FutureWorkInterestsEntity(
  reference = reference,
  interests = interests,
  createdAtPrison = createdAtPrison,
  updatedAtPrison = updatedAtPrison,
).apply {
  this.id = id
  this.createdAt = createdAt
  this.createdBy = createdBy
  this.updatedAt = updatedAt
  this.updatedBy = updatedBy
}

fun aValidFutureWorkInterestsEntityWithJpaFieldsPopulated(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  interests: MutableList<WorkInterestEntity> = mutableListOf(aValidWorkInterestEntity()),
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String? = "asmith_gen",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
) = FutureWorkInterestsEntity(
  reference = reference,
  interests = interests,
  createdAtPrison = createdAtPrison,
  updatedAtPrison = updatedAtPrison,
).apply {
  this.id = id
  this.createdAt = createdAt
  this.createdBy = createdBy
  this.updatedAt = updatedAt
  this.updatedBy = updatedBy
}

fun aValidWorkInterestEntity(
  reference: UUID = UUID.randomUUID(),
  workType: WorkInterestType = WorkInterestType.OTHER,
  workTypeOther: String? = "Any job I can get",
  role: String? = "Any role",
) = WorkInterestEntity(
  reference = reference,
  workType = workType,
  workTypeOther = workTypeOther,
  role = role,
)
