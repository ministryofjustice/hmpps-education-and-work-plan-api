package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.EntityType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.UpdateNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.aValidCreateNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.aValidNoteDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.aValidNoteEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.note.NoteMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.NoteRepository
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class JpaNotePersistenceAdapterTest {

  @InjectMocks
  private lateinit var persistenceAdapter: JpaNotePersistenceAdapter

  @Mock
  private lateinit var noteRepository: NoteRepository

  @Test
  fun `createNote should save note and return NoteDto`() {
    // given
    val createNoteDto = aValidCreateNoteDto()
    val createNoteEntity = NoteMapper.toEntity(createNoteDto)
    val expected = NoteMapper.toModel(createNoteEntity)

    given(noteRepository.save(any<NoteEntity>())).willReturn(createNoteEntity)

    // when
    val result = persistenceAdapter.createNote(createNoteDto)

    // then
    verify(noteRepository, times(1)).save(any())
    Assertions.assertThat(result).isEqualTo(expected)
  }

  @Test
  fun `getNotes should return list of NoteDto`() {
    // given
    val entityReference = UUID.randomUUID()
    val entityType = EntityType.GOAL
    val noteDTO = aValidNoteDto()
    val noteEntity = aValidNoteEntity(content = noteDTO.content)

    given(
      noteRepository.findAllByEntityReferenceAndEntityType(
        entityReference,
        NoteMapper.toEntity(entityType),
      ),
    ).willReturn(
      listOf(noteEntity),
    )

    // when
    val result = persistenceAdapter.getNotes(entityReference, entityType)

    // then
    Assertions.assertThat(result[0].content).isEqualTo(noteDTO.content)
    verify(noteRepository, times(1)).findAllByEntityReferenceAndEntityType(
      entityReference,
      NoteMapper.toEntity(entityType),
    )
  }

  @Test
  fun `updateNote should update note and return updated NoteDto`() {
    // Given
    val updateNoteDto = UpdateNoteDto(UUID.randomUUID(), "new content", "some prison")
    val noteEntity = aValidNoteEntity()
    val expected = NoteMapper.toModel(noteEntity)
      .copy(content = updateNoteDto.content!!, lastUpdatedAtPrison = updateNoteDto.lastUpdatedAtPrison)

    given(noteRepository.findByReference(updateNoteDto.reference)).willReturn(noteEntity)
    given(noteRepository.save(any<NoteEntity>())).willReturn(noteEntity)

    // When
    val result = persistenceAdapter.updateNote(updateNoteDto)

    // Then
    verify(noteRepository, times(1)).save(any())
    Assertions.assertThat(result).isEqualTo(expected)
  }
}
