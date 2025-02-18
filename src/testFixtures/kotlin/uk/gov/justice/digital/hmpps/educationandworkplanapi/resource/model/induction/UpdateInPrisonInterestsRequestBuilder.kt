package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonWorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateInPrisonInterestsRequest
import java.util.UUID

fun aValidUpdateInPrisonInterestsRequest(
  reference: UUID? = UUID.randomUUID(),
  inPrisonWorkInterests: List<InPrisonWorkInterest> = listOf(aValidInPrisonWorkInterest()),
  inPrisonTrainingInterests: List<InPrisonTrainingInterest> = listOf(aValidInPrisonTrainingInterest()),
): UpdateInPrisonInterestsRequest = UpdateInPrisonInterestsRequest(
  reference = reference,
  inPrisonWorkInterests = inPrisonWorkInterests,
  inPrisonTrainingInterests = inPrisonTrainingInterests,
)
