package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.note

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.NoteResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.NoteType
import java.time.OffsetDateTime
import java.util.UUID

fun aValidNoteResponse(
  reference: UUID = UUID.randomUUID(),
  content: String = "Some notes about the goal",
  type: NoteType = NoteType.GOAL,
  createdBy: String = "asmith_gen",
  createdByDisplayName: String = "Alex Smith",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "asmith_gen",
  updatedByDisplayName: String = "Alex Smith",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedAtPrison: String = "BXI",
): NoteResponse =
  NoteResponse(
    reference = reference,
    content = content,
    type = type,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    updatedBy = updatedBy,
    updatedByDisplayName = updatedByDisplayName,
    updatedAt = updatedAt,
    updatedAtPrison = updatedAtPrison,
  )
