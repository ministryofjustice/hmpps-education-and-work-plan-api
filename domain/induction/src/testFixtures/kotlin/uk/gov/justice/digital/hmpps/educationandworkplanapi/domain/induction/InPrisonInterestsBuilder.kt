package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

fun aValidInPrisonInterests(
  inPrisonWorkInterests: List<InPrisonWorkInterest> = listOf(aValidInPrisonWorkInterest()),
  inPrisonTrainingInterests: List<InPrisonTrainingInterest> = listOf(aValidInPrisonTrainingInterest()),
) = InPrisonInterests(
  inPrisonWorkInterests = inPrisonWorkInterests,
  inPrisonTrainingInterests = inPrisonTrainingInterests,
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
