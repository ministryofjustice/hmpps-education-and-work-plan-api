package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidWorkInterest
import java.util.UUID

fun aValidCreateFutureWorkInterestsDto(
  interests: List<WorkInterest> = listOf(aValidWorkInterest()),
  prisonId: String = "BXI",
) =
  CreateFutureWorkInterestsDto(
    interests = interests,
    prisonId = prisonId,
  )

fun aValidUpdateFutureWorkInterestsDto(
  reference: UUID = UUID.randomUUID(),
  interests: List<WorkInterest> = listOf(aValidWorkInterest()),
  prisonId: String = "BXI",
) =
  UpdateFutureWorkInterestsDto(
    reference = reference,
    interests = interests,
    prisonId = prisonId,
  )
