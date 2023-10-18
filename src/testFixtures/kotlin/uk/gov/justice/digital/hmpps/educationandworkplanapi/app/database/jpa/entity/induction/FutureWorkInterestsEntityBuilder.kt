package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import java.util.UUID

fun aValidFutureWorkInterestsEntity(
  reference: UUID = UUID.randomUUID(),
  interests: List<WorkInterestEntity> = listOf(aValidWorkInterestEntity()),
  createdAtPrison: String = "BXI",
  updatedAtPrison: String = "BXI",
) =
  FutureWorkInterestsEntity(
    reference = reference,
    interests = interests,
    createdAtPrison = createdAtPrison,
    updatedAtPrison = updatedAtPrison,
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
