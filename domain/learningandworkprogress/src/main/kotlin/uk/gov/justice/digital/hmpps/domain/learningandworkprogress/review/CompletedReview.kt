package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteDto
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * A prisoner's completed Review.
 */
data class CompletedReview(
  val reference: UUID,
  val prisonNumber: String,
  val deadlineDate: LocalDate,
  val completedDate: LocalDate,
  val note: NoteDto,
  val createdBy: String,
  val createdAt: Instant,
  val createdAtPrison: String,
  val conductedBy: ReviewConductedBy?,
)

data class ReviewConductedBy(
  val name: String,
  val role: String,
)
