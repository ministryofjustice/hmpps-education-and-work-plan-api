package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePreviousTrainingRequest
import java.util.UUID

fun aValidUpdatePreviousTrainingRequest(
  reference: UUID? = UUID.randomUUID(),
  trainingTypes: List<TrainingType> = listOf(TrainingType.OTHER),
  trainingTypeOther: String? = "Certified Kotlin Developer",
): UpdatePreviousTrainingRequest = UpdatePreviousTrainingRequest(
  reference = reference,
  trainingTypes = trainingTypes,
  trainingTypeOther = trainingTypeOther,
)
