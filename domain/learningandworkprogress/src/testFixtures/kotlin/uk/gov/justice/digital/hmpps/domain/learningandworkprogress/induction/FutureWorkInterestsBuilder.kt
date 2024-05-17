package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction

import java.time.Instant
import java.util.UUID

fun aValidFutureWorkInterests(
  reference: UUID = UUID.randomUUID(),
  interests: List<WorkInterest> = listOf(aValidWorkInterest()),
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  lastUpdatedBy: String? = "bjones_gen",
  lastUpdatedByDisplayName: String? = "Barry Jones",
  lastUpdatedAt: Instant? = Instant.now(),
  lastUpdatedAtPrison: String = "BXI",
) =
  FutureWorkInterests(
    reference = reference,
    interests = interests,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedByDisplayName = lastUpdatedByDisplayName,
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
