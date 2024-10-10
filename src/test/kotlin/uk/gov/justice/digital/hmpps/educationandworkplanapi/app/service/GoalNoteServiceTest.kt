package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.CreateNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.EntityType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.UpdateNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.aValidNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.assertThat
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.service.NoteService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidGoal
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class GoalNoteServiceTest {

  @Mock
  private lateinit var noteService: NoteService

  @InjectMocks
  private lateinit var goalNoteService: GoalNoteService

  @Test
  fun `createNotes should call createNote for each goal with a populated goal note`() {
    // Given
    val prisonNumber = "A1234BC"
    val goal1 = aValidGoal(
      notes = "The notes for goal 1",
    )
    val goal2 = aValidGoal(
      notes = "The notes for goal 2",
    )
    val goal3 = aValidGoal(
      notes = "",
    )
    val goal4 = aValidGoal(
      notes = null,
    )

    val createdGoals = listOf(goal1, goal2, goal3, goal4)

    // When
    goalNoteService.createNotes(prisonNumber, createdGoals)

    // Then
    val captor = argumentCaptor<CreateNoteDto>()
    verify(noteService, times(2)).createNote(captor.capture())
    assertThat(captor.firstValue)
      .hasPrisonNumber(prisonNumber)
      .hasEntityReference(goal1.reference)
      .hasEntityType(EntityType.GOAL)
      .hasContent(goal1.notes!!)
    assertThat(captor.secondValue)
      .hasPrisonNumber(prisonNumber)
      .hasEntityReference(goal2.reference)
      .hasEntityType(EntityType.GOAL)
      .hasContent(goal2.notes!!)
  }

  @Test
  fun `getNotes should return content of first note`() {
    // Given
    val entityReference = UUID.randomUUID()
    val notes = listOf(
      aValidNoteDto(reference = UUID.randomUUID(), content = "First note"),
      aValidNoteDto(reference = UUID.randomUUID(), content = "Second note"),
    )

    given(noteService.getNotes(entityReference, EntityType.GOAL, NoteType.GOAL)).willReturn(notes)

    // When
    val result = goalNoteService.getNotes(entityReference)

    // Then
    assert(result == "First note")
    verify(noteService, times(1)).getNotes(entityReference, EntityType.GOAL, NoteType.GOAL)
  }

  @Test
  fun `updateNotes should update note when note exists`() {
    // Given
    val entityReference = UUID.randomUUID()
    val lastUpdatedAtPrison = "Prison A"
    val updatedText = "Updated note content"
    val existingNote = aValidNoteDto(reference = UUID.randomUUID(), content = "Old content")

    given(noteService.getNotes(entityReference, EntityType.GOAL, NoteType.GOAL)).willReturn(listOf(existingNote))

    // When
    goalNoteService.updateNotes(entityReference, lastUpdatedAtPrison, updatedText)

    // Then
    verify(noteService, times(1)).updateNote(
      UpdateNoteDto(
        reference = existingNote.reference,
        content = updatedText,
        lastUpdatedAtPrison = lastUpdatedAtPrison,
      ),
    )
  }

  @Test
  fun `updateNotes should not update note when no note exists`() {
    // Given
    val entityReference = UUID.randomUUID()
    val lastUpdatedAtPrison = "Prison A"
    val updatedText = "Updated note content"

    given(noteService.getNotes(entityReference, EntityType.GOAL, NoteType.GOAL)).willReturn(emptyList())

    // When
    goalNoteService.updateNotes(entityReference, lastUpdatedAtPrison, updatedText)

    // Then
    verify(noteService, never()).updateNote(any())
  }
}
