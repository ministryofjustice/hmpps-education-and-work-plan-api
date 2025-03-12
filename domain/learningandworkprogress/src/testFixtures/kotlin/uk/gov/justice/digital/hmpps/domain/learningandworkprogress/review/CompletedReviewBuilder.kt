package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.aValidNoteDto
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun aValidCompletedReview(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = randomValidPrisonNumber(),
  deadlineDate: LocalDate = LocalDate.now().plusMonths(1),
  completedDate: LocalDate = LocalDate.now(),
  note: NoteDto = aValidNoteDto(),
  createdBy: String = "asmith_gen",
  createdAt: Instant = Instant.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "bjones_gen",
  updatedAt: Instant = Instant.now(),
  updatedAtPrison: String = "BXI",
  conductedBy: ReviewConductedBy? = ReviewConductedBy(name = "Barnie Jones", role = "Peer mentor"),
  reviewScheduleReference: UUID? = null,
  preRelease: Boolean = false,
): CompletedReview =
  CompletedReview(
    reference = reference,
    prisonNumber = prisonNumber,
    deadlineDate = deadlineDate,
    completedDate = completedDate,
    note = note,
    createdBy = createdBy,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    conductedBy = conductedBy,
    reviewScheduleReference = reviewScheduleReference,
    updatedBy = updatedBy,
    updatedAt = updatedAt,
    updatedAtPrison = updatedAtPrison,
    preRelease = preRelease,
  )
