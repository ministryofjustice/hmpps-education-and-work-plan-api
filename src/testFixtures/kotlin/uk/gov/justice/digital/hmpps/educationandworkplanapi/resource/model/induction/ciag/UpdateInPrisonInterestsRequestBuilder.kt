package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateInPrisonInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonTrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonWorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonWorkType

fun aValidCreateInPrisonInterestsRequest(
  inPrisonWorkInterests: List<InPrisonWorkInterest> = listOf(aValidInPrisonWorkInterest()),
  inPrisonTrainingInterests: List<InPrisonTrainingInterest> = listOf(aValidInPrisonTrainingInterest()),
): CreateInPrisonInterestsRequest =
  CreateInPrisonInterestsRequest(
    inPrisonWorkInterests = inPrisonWorkInterests,
    inPrisonTrainingInterests = inPrisonTrainingInterests,
  )

fun aValidInPrisonWorkInterest(
  workType: InPrisonWorkType = InPrisonWorkType.OTHER,
  workTypeOther: String? = "Any in-prison work",
): InPrisonWorkInterest =
  InPrisonWorkInterest(
    workType = workType,
    workTypeOther = workTypeOther,
  )

fun aValidInPrisonTrainingInterest(
  trainingType: InPrisonTrainingType = InPrisonTrainingType.OTHER,
  trainingTypeOther: String? = "Any in-prison training",
): InPrisonTrainingInterest =
  InPrisonTrainingInterest(
    trainingType = trainingType,
    trainingTypeOther = trainingTypeOther,
  )
