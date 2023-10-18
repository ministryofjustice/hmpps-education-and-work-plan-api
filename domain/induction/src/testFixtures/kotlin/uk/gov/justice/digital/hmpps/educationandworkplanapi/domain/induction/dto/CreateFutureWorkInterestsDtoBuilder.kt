package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidWorkInterest

fun aValidCreateFutureWorkInterestsDto(
  interests: List<WorkInterest> = listOf(aValidWorkInterest()),
  prisonId: String = "BXI",
) =
  CreateFutureWorkInterestsDto(
    interests = interests,
    prisonId = prisonId,
  )
