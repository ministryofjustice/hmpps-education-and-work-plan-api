package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidWorkInterest
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
