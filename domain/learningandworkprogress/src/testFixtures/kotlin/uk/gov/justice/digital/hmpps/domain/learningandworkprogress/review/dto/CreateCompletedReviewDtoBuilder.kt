package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import java.time.LocalDate

fun aValidCreateCompletedReviewDto(
  prisonNumber: String = randomValidPrisonNumber(),
  prisonId: String = "BXI",
  note: String = "Note content",
  conductedAt: LocalDate = LocalDate.now(),
  conductedBy: String? = "Barnie Jones",
  conductedByRole: String? = "Peer mentor",
  prisonerReleaseDate: LocalDate? = LocalDate.now().plusYears(1),
  prisonerSentenceType: SentenceType = SentenceType.SENTENCED,
  prisonerHasIndeterminateFlag: Boolean = false,
  prisonerHasRecallFlag: Boolean = false,
): CreateCompletedReviewDto =
  CreateCompletedReviewDto(
    prisonNumber = prisonNumber,
    prisonId = prisonId,
    note = note,
    conductedAt = conductedAt,
    conductedBy = conductedBy,
    conductedByRole = conductedByRole,
    prisonerReleaseDate = prisonerReleaseDate,
    prisonerSentenceType = prisonerSentenceType,
    prisonerHasIndeterminateFlag = prisonerHasIndeterminateFlag,
    prisonerHasRecallFlag = prisonerHasRecallFlag,
  )
