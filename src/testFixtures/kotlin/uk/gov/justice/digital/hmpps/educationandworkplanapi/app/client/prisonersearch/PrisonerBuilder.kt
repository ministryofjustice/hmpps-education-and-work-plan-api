package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch

import uk.gov.justice.digital.hmpps.educationandworkplanapi.randomValidPrisonNumber
import java.time.LocalDate

fun aValidPrisoner(
  prisonerNumber: String = randomValidPrisonNumber(),
  legalStatus: LegalStatus = LegalStatus.SENTENCED,
  releaseDate: LocalDate? = LocalDate.now().plusYears(1),
  prisonId: String? = "BXI",
  isIndeterminateSentence: Boolean = false,
  isRecall: Boolean = false,
  firstName: String = "Bob",
  lastName: String = "Smith",
  cellLocation: String = "B-2-022",
  dateOfBirth: LocalDate = LocalDate.now().minusYears(20),
  releaseType: String = "ARD",
  receptionDate: LocalDate = LocalDate.now().minusYears(1),
  sentenceStartDate: LocalDate = LocalDate.now().minusYears(1),
): Prisoner = Prisoner(
  prisonerNumber = prisonerNumber,
  legalStatus = legalStatus,
  releaseDate = releaseDate,
  prisonId = prisonId,
  isRecall = isRecall,
  isIndeterminateSentence = isIndeterminateSentence,
  firstName = firstName,
  lastName = lastName,
  cellLocation = cellLocation,
  dateOfBirth = dateOfBirth,
  releaseType = releaseType,
  receptionDate = receptionDate,
  sentenceStartDate = sentenceStartDate,
)
