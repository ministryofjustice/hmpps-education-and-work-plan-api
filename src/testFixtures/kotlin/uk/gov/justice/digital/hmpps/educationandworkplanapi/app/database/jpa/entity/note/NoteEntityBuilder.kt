package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import java.util.UUID

fun aValidNoteEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID? = UUID.randomUUID(),
  prisonNumber: String = aValidPrisonNumber(),
  content: String = "Note content",
  entityReference: UUID = UUID.randomUUID(),
  entityType: EntityType = EntityType.GOAL,
  noteType: NoteType = NoteType.GOAL,
  createdAtPrison: String = "BXI",
  updatedAtPrison: String = "MDI",
): NoteEntity =
  NoteEntity(
    id = id,
    reference = reference,
    prisonNumber = prisonNumber,
    content = content,
    entityReference = entityReference,
    entityType = entityType,
    noteType = noteType,
    createdAtPrison = createdAtPrison,
    updatedAtPrison = updatedAtPrison,
  )
