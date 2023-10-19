package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

import java.time.Instant

fun aValidPreviousTraining(
  trainingTypes: List<TrainingType> = listOf(TrainingType.CSCS_CARD),
  trainingTypeOther: String? = null,
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  createdAt: Instant? = Instant.now(),
  lastUpdatedBy: String? = "bjones_gen",
  lastUpdatedByDisplayName: String? = "Barry Jones",
  lastUpdatedAt: Instant? = Instant.now(),
) =
  PreviousTraining(
    trainingTypes = trainingTypes,
    trainingTypeOther = trainingTypeOther,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedByDisplayName = lastUpdatedByDisplayName,
    lastUpdatedAt = lastUpdatedAt,
  )
