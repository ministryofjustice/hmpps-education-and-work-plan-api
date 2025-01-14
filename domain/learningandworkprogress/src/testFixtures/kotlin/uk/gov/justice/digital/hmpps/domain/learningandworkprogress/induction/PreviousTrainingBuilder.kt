package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction

import java.time.Instant
import java.util.UUID

fun aValidPreviousTraining(
  reference: UUID = UUID.randomUUID(),
  trainingTypes: List<TrainingType> = listOf(TrainingType.CSCS_CARD),
  trainingTypeOther: String? = null,
  createdBy: String = "asmith_gen",
  createdAt: Instant = Instant.now(),
  createdAtPrison: String = "BXI",
  lastUpdatedBy: String = "bjones_gen",
  lastUpdatedAt: Instant = Instant.now(),
  lastUpdatedAtPrison: String = "BXI",
) =
  PreviousTraining(
    reference = reference,
    trainingTypes = trainingTypes,
    trainingTypeOther = trainingTypeOther,
    createdBy = createdBy,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedAt = lastUpdatedAt,
    lastUpdatedAtPrison = lastUpdatedAtPrison,
  )
