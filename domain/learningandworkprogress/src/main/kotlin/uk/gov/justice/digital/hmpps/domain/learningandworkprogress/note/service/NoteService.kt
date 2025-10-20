package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.CreateNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.EntityType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.UpdateNoteDto
import java.util.UUID

class NoteService(private val notePersistenceAdapter: NotePersistenceAdapter) {
  fun createNote(createNoteDto: CreateNoteDto): NoteDto = notePersistenceAdapter.createNote(createNoteDto)

  fun getNotes(entityReference: UUID, entityType: EntityType): List<NoteDto> = notePersistenceAdapter.getNotes(entityReference, entityType)

  fun getNotes(entityReference: UUID, entityType: EntityType, noteType: NoteType): List<NoteDto> = notePersistenceAdapter.getNotes(entityReference, entityType, noteType)

  fun updateNote(updateNoteDto: UpdateNoteDto): NoteDto = notePersistenceAdapter.updateNote(updateNoteDto)

  fun deleteNote(entityReference: UUID, entityType: EntityType, noteType: NoteType) {
    notePersistenceAdapter.deleteNoteByEntityReference(entityReference, entityType, noteType)
  }
}
