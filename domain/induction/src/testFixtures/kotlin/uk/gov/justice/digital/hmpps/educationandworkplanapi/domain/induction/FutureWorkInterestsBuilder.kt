package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

import java.time.Instant

fun aValidFutureWorkInterests(
  interests: List<WorkInterest> = listOf(aValidWorkInterest()),
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  createdAt: Instant? = Instant.now(),
  lastUpdatedBy: String? = "bjones_gen",
  lastUpdatedByDisplayName: String? = "Barry Jones",
  lastUpdatedAt: Instant? = Instant.now(),
) =
  FutureWorkInterests(
    interests = interests,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedByDisplayName = lastUpdatedByDisplayName,
    lastUpdatedAt = lastUpdatedAt,
  )

fun aValidWorkInterest(
  workType: WorkInterestType = WorkInterestType.CONSTRUCTION,
  workTypeOther: String? = null,
  role: String = "Bricklaying",
) = WorkInterest(
  workType = workType,
  workTypeOther = workTypeOther,
  role = role,
)
