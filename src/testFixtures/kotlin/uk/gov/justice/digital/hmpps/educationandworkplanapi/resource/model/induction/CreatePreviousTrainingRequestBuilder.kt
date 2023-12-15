package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousTrainingRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TrainingType

fun aValidCreatePreviousTrainingRequest(
  trainingTypes: List<TrainingType> = listOf(TrainingType.OTHER),
  trainingTypeOther: String? = "Certified Kotlin Developer",
): CreatePreviousTrainingRequest =
  CreatePreviousTrainingRequest(
    trainingTypes = trainingTypes,
    trainingTypeOther = trainingTypeOther,
  )
