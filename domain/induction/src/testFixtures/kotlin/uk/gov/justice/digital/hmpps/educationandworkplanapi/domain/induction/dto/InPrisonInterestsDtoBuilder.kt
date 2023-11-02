package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonWorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidInPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidInPrisonWorkInterest
import java.util.UUID

fun aValidCreateInPrisonInterestsDto(
  inPrisonWorkInterests: List<InPrisonWorkInterest> = listOf(aValidInPrisonWorkInterest()),
  inPrisonTrainingInterests: List<InPrisonTrainingInterest> = listOf(aValidInPrisonTrainingInterest()),
  prisonId: String = "BXI",
) = CreateInPrisonInterestsDto(
  inPrisonWorkInterests = inPrisonWorkInterests,
  inPrisonTrainingInterests = inPrisonTrainingInterests,
  prisonId = prisonId,
)

fun aValidUpdateInPrisonInterestsDto(
  reference: UUID = UUID.randomUUID(),
  inPrisonWorkInterests: List<InPrisonWorkInterest> = listOf(aValidInPrisonWorkInterest()),
  inPrisonTrainingInterests: List<InPrisonTrainingInterest> = listOf(aValidInPrisonTrainingInterest()),
  prisonId: String = "BXI",
) = UpdateInPrisonInterestsDto(
  reference = reference,
  inPrisonWorkInterests = inPrisonWorkInterests,
  inPrisonTrainingInterests = inPrisonTrainingInterests,
  prisonId = prisonId,
)
