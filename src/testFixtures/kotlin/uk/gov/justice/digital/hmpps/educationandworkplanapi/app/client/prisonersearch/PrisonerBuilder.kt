package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import java.time.LocalDate

fun aValidPrisoner(
  prisonerNumber: String = aValidPrisonNumber(),
  legalStatus: LegalStatus = LegalStatus.SENTENCED,
  releaseDate: LocalDate? = LocalDate.now().plusYears(1),
  prisonId: String? = "BXI",
  isIndeterminateSentence: Boolean = false,
  isRecall: Boolean = false,
): Prisoner = Prisoner(
  prisonerNumber = prisonerNumber,
  legalStatus = legalStatus,
  releaseDate = releaseDate,
  prisonId = prisonId,
  isRecall = isRecall,
  isIndeterminateSentence = isIndeterminateSentence,
)
