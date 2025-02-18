package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import java.time.Instant
import java.util.UUID

fun aValidNoteEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = aValidPrisonNumber(),
  content: String = "Note content",
  entityReference: UUID = UUID.randomUUID(),
  entityType: EntityType = EntityType.GOAL,
  noteType: NoteType = NoteType.GOAL,
  createdBy: String = "asmith_gen",
  createdAt: Instant = Instant.now(),
  createdAtPrison: String = "BXI",
  updatedAt: Instant = Instant.now(),
  updatedBy: String = "bjones_gen",
  updatedAtPrison: String = "BXI",
): NoteEntity = NoteEntity(
  reference = reference,
  prisonNumber = prisonNumber,
  content = content,
  entityReference = entityReference,
  entityType = entityType,
  noteType = noteType,
  createdAtPrison = createdAtPrison,
  updatedAtPrison = updatedAtPrison,
).apply {
  this.id = id
  this.createdAt = createdAt
  this.createdBy = createdBy
  this.updatedAt = updatedAt
  this.updatedBy = updatedBy
}
