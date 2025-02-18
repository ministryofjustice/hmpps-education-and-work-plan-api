package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousTrainingResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TrainingType
import java.time.OffsetDateTime
import java.util.UUID

fun aValidPreviousTrainingResponse(
  reference: UUID = UUID.randomUUID(),
  trainingTypes: List<TrainingType> = listOf(TrainingType.OTHER),
  trainingTypeOther: String? = "Certified Kotlin Developer",
  createdBy: String = "asmith_gen",
  createdByDisplayName: String = "Alex Smith",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "asmith_gen",
  updatedByDisplayName: String = "Alex Smith",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedAtPrison: String = "BXI",
): PreviousTrainingResponse = PreviousTrainingResponse(
  reference = reference,
  trainingTypes = trainingTypes,
  trainingTypeOther = trainingTypeOther,
  createdBy = createdBy,
  createdByDisplayName = createdByDisplayName,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  updatedBy = updatedBy,
  updatedByDisplayName = updatedByDisplayName,
  updatedAt = updatedAt,
  updatedAtPrison = updatedAtPrison,
)
