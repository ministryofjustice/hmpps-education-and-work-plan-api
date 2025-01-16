package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction

import java.time.Instant
import java.util.UUID

fun aValidFutureWorkInterests(
  reference: UUID = UUID.randomUUID(),
  interests: List<WorkInterest> = listOf(aValidWorkInterest()),
  createdBy: String = "asmith_gen",
  createdAt: Instant = Instant.now(),
  createdAtPrison: String = "BXI",
  lastUpdatedBy: String = "bjones_gen",
  lastUpdatedAt: Instant = Instant.now(),
  lastUpdatedAtPrison: String = "BXI",
) =
  FutureWorkInterests(
    reference = reference,
    interests = interests,
    createdBy = createdBy,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedAt = lastUpdatedAt,
    lastUpdatedAtPrison = lastUpdatedAtPrison,
  )

fun aValidWorkInterest(
  workType: WorkInterestType = WorkInterestType.CONSTRUCTION,
  workTypeOther: String? = null,
  role: String? = "Bricklaying",
) = WorkInterest(
  workType = workType,
  workTypeOther = workTypeOther,
  role = role,
)
