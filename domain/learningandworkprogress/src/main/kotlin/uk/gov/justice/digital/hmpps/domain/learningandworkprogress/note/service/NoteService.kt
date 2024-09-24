package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.CreateNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.EntityType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.UpdateNoteDto
import java.util.UUID

class NoteService(private val notePersistenceAdapter: NotePersistenceAdapter) {
  fun createNote(createNoteDto: CreateNoteDto): NoteDto {
    return notePersistenceAdapter.createNote(createNoteDto)
  }

  fun getNotes(entityReference: UUID, entityType: EntityType): List<NoteDto> {
    return notePersistenceAdapter.getNotes(entityReference, entityType)
  }

  fun updateNote(updateNoteDto: UpdateNoteDto): NoteDto {
    return notePersistenceAdapter.updateNote(updateNoteDto)
  }
}
