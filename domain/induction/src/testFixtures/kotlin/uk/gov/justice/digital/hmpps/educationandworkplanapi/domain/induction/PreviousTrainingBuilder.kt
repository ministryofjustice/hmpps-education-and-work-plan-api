package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

fun aValidPreviousTraining(
  trainingType: List<TrainingType> = listOf(TrainingType.CSCS_CARD),
  trainingTypeOther: String? = null,
) =
  PreviousTraining(
    trainingType = trainingType,
    trainingTypeOther = trainingTypeOther,
  )
