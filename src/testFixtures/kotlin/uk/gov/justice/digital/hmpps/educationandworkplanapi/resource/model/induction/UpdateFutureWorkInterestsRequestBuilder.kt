package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.FutureWorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateFutureWorkInterestsRequest
import java.util.UUID

fun aValidUpdateFutureWorkInterestsRequest(
  reference: UUID? = UUID.randomUUID(),
  interests: List<FutureWorkInterest> = listOf(aValidFutureWorkInterest()),
): UpdateFutureWorkInterestsRequest =
  UpdateFutureWorkInterestsRequest(reference = reference, interests = interests)
