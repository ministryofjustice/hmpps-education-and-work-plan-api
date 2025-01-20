package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto

import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import java.time.Instant
import java.util.UUID

fun aValidCreateNoteDto(
  prisonNumber: String = aValidPrisonNumber(),
  content: String = "Note content",
  entityReference: UUID = UUID.randomUUID(),
  entityType: EntityType = EntityType.GOAL,
  noteType: NoteType = NoteType.GOAL,
  createdAtPrison: String = "BXI",
  lastUpdatedAtPrison: String = "MDI",
): CreateNoteDto =
  CreateNoteDto(
    prisonNumber = prisonNumber,
    content = content,
    entityReference = entityReference,
    entityType = entityType,
    noteType = noteType,
    createdAtPrison = createdAtPrison,
    lastUpdatedAtPrison = lastUpdatedAtPrison,
  )

fun aValidNoteDto(
  content: String = "Note content",
  reference: UUID = UUID.randomUUID(),
  entityReference: UUID = UUID.randomUUID(),
  entityType: EntityType = EntityType.GOAL,
  noteType: NoteType = NoteType.GOAL,
  createdBy: String = "asmith_gen",
  createdAt: Instant = Instant.now(),
  createdAtPrison: String = "BXI",
  lastUpdatedBy: String = "bjones_gen",
  lastUpdatedAt: Instant = Instant.now(),
  lastUpdatedAtPrison: String = "MDI",
): NoteDto =
  NoteDto(
    content = content,
    reference = reference,
    entityReference = entityReference,
    entityType = entityType,
    noteType = noteType,
    createdAtPrison = createdAtPrison,
    lastUpdatedAtPrison = lastUpdatedAtPrison,
    createdAt = createdAt,
    lastUpdatedAt = lastUpdatedAt,
    createdBy = createdBy,
    lastUpdatedBy = lastUpdatedBy,
  )
