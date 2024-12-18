package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType
import java.time.LocalDate

data class CreateInitialReviewScheduleDto(
  val prisonNumber: String,
  val prisonId: String,
  val prisonerReleaseDate: LocalDate?,
  val prisonerSentenceType: SentenceType,
  val prisonerHasIndeterminateFlag: Boolean,
  val prisonerHasRecallFlag: Boolean,
  val isTransfer: Boolean,
  val isReadmission: Boolean,
)
