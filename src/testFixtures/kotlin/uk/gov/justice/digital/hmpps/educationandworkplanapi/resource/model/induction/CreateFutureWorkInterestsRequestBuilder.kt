package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateFutureWorkInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.FutureWorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType

fun aValidCreateFutureWorkInterestsRequest(
  interests: List<FutureWorkInterest> = listOf(aValidFutureWorkInterest()),
): CreateFutureWorkInterestsRequest =
  CreateFutureWorkInterestsRequest(interests = interests)

fun aValidFutureWorkInterest(
  workType: WorkType = WorkType.OTHER,
  workTypeOther: String? = "Any job I can get",
  role: String? = "Any role",
): FutureWorkInterest =
  FutureWorkInterest(
    workType = workType,
    workTypeOther = workTypeOther,
    role = role,
  )
