package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto

import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType
import java.time.LocalDate

fun aValidCreateCompletedReviewDto(
  prisonNumber: String = aValidPrisonNumber(),
  prisonId: String = "BXI",
  note: String = "Note content",
  conductedAt: LocalDate = LocalDate.now(),
  conductedBy: String? = "Barnie Jones",
  conductedByRole: String? = "Peer mentor",
  prisonerReleaseDate: LocalDate = LocalDate.now().plusYears(1),
  prisonerSentenceType: SentenceType = SentenceType.SENTENCED,
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
  )
