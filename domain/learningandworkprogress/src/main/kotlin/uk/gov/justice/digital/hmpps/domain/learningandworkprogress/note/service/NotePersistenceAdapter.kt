package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.CreateNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.EntityType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.UpdateNoteDto
import java.util.*

/**
 * Persistence Adapter for [Note] instances.
 *
 * Implementations should use the underlying persistence of the application in question, eg: JPA, Mongo, Dynamo, Redis etc
 *
 * Implementations should not throw exceptions. These are not part of the interface and are not checked or handled by
 * [NoteService].
 */
interface NotePersistenceAdapter {

  /**
   * Creates new [Note] for the associated entity.
   */
  fun createNote(createNoteDto: CreateNoteDto): NoteDto

  /**
   * Gets the notes associated with this entity reference.
   */
  fun getNotes(entityReference: UUID, entityType: EntityType): List<NoteDto>

  /**
   * update a [Note].
   */
  fun updateNote(updateNoteDto: UpdateNoteDto): NoteDto
}
