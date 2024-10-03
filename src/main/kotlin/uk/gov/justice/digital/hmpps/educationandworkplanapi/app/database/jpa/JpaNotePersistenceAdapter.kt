package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.CreateNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.EntityType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.UpdateNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.service.NotePersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.note.NoteMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.NoteRepository
import java.util.UUID

@Component
class JpaNotePersistenceAdapter(
  private val noteRepository: NoteRepository,
) : NotePersistenceAdapter {

  @Transactional
  override fun createNote(createNoteDto: CreateNoteDto): NoteDto {
    val noteEntity = noteRepository.save(NoteMapper.toEntity(createNoteDto))
    return NoteMapper.toModel(noteEntity)
  }

  override fun getNotes(entityReference: UUID, entityType: EntityType, noteType: NoteType): List<NoteDto> {
    return noteRepository.findAllByEntityReferenceAndEntityTypeAndNoteType(entityReference, NoteMapper.toEntity(entityType), NoteMapper.toEntity(noteType)).map { NoteMapper.toModel(it) }
  }

  override fun updateNote(updateNoteDto: UpdateNoteDto): NoteDto {
    val noteEntity = noteRepository.findByReference(updateNoteDto.reference)
    noteEntity.content = updateNoteDto.content
    noteEntity.updatedAtPrison = updateNoteDto.lastUpdatedAtPrison
    noteRepository.save(noteEntity)
    return NoteMapper.toModel(noteEntity)
  }
}
