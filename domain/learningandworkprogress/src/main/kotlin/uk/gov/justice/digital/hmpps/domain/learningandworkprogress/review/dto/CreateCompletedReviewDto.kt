package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType
import java.time.LocalDate

data class CreateCompletedReviewDto(
  val prisonNumber: String,
  val prisonId: String,
  val note: String,
  val conductedAt: LocalDate,
  val conductedBy: String?,
  val conductedByRole: String?,
  val prisonerReleaseDate: LocalDate?,
  val prisonerSentenceType: SentenceType,
)
