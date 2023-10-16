package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

import java.time.Instant

fun aValidInPrisonInterests(
  inPrisonWorkInterests: List<InPrisonWorkInterest> = listOf(aValidInPrisonWorkInterest()),
  inPrisonTrainingInterests: List<InPrisonTrainingInterest> = listOf(aValidInPrisonTrainingInterest()),
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  createdAt: Instant? = Instant.now(),
  lastUpdatedBy: String? = "bjones_gen",
  lastUpdatedByDisplayName: String? = "Barry Jones",
  lastUpdatedAt: Instant? = Instant.now(),
) = InPrisonInterests(
  inPrisonWorkInterests = inPrisonWorkInterests,
  inPrisonTrainingInterests = inPrisonTrainingInterests,
  createdBy = createdBy,
  createdByDisplayName = createdByDisplayName,
  createdAt = createdAt,
  lastUpdatedBy = lastUpdatedBy,
  lastUpdatedByDisplayName = lastUpdatedByDisplayName,
  lastUpdatedAt = lastUpdatedAt,
)

fun aValidInPrisonWorkInterest(
  workType: InPrisonWorkType = InPrisonWorkType.PRISON_LAUNDRY,
  workTypeOther: String? = null,
) = InPrisonWorkInterest(
  workType = workType,
  workTypeOther = workTypeOther,
)

fun aValidInPrisonTrainingInterest(
  trainingType: InPrisonTrainingType = InPrisonTrainingType.CATERING,
  trainingTypeOther: String? = null,
) =
  InPrisonTrainingInterest(
    trainingType = trainingType,
    trainingTypeOther = trainingTypeOther,
  )
