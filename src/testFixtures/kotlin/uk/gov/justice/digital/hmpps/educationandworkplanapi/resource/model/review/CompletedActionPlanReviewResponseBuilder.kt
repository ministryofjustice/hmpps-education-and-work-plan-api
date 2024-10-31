package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CompletedActionPlanReviewResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.NoteResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.note.aValidNoteResponse
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

fun aValidCompletedActionPlanReviewResponse(
  reference: UUID = UUID.randomUUID(),
  deadlineDate: LocalDate = LocalDate.now().plusMonths(1),
  completedDate: LocalDate = LocalDate.now(),
  note: NoteResponse = aValidNoteResponse(),
  createdBy: String = "asmith_gen",
  createdByDisplayName: String = "Alex Smith",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  conductedBy: String? = "Barnie Jones",
  conductedByRole: String? = "Peer mentor",
): CompletedActionPlanReviewResponse =
  CompletedActionPlanReviewResponse(
    reference = reference,
    deadlineDate = deadlineDate,
    completedDate = completedDate,
    note = note,
    conductedBy = conductedBy,
    conductedByRole = conductedByRole,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
  )
