package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.CreateNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.EntityType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.UpdateNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.service.NoteService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.GoalNotesService
import java.util.UUID

@Component
class GoalNoteService(
  private val noteService: NoteService,
) : GoalNotesService {

  override fun createNotes(prisonNumber: String, createdGoals: List<Goal>) {
    // This copy is created to prevent a concurrent modification exception which sometimes happens.
    val goalsCopy = ArrayList(createdGoals)
    goalsCopy.forEach { createdGoal ->
      if (!createdGoal.notes.isNullOrEmpty()) {
        noteService.createNote(createdGoal.mapToNotesDTO(prisonNumber))
      }
    }
  }

  override fun getNotes(entityReference: UUID): String? = noteService.getNotes(entityReference, EntityType.GOAL, NoteType.GOAL).firstOrNull()?.content

  override fun deleteNote(entityReference: UUID) {
    noteService.deleteNote(entityReference, EntityType.GOAL, NoteType.GOAL)
  }

  override fun updateNotes(entityReference: UUID, lastUpdatedAtPrison: String, updatedText: String) {
    val note = noteService.getNotes(entityReference, EntityType.GOAL, NoteType.GOAL).firstOrNull()
    // If no note exists, return early
    note?.let {
      noteService.updateNote(
        UpdateNoteDto(
          reference = it.reference,
          content = updatedText,
          lastUpdatedAtPrison = lastUpdatedAtPrison,
        ),
      )
    }
  }
}

fun Goal.mapToNotesDTO(prisonNumber: String): CreateNoteDto = CreateNoteDto(
  prisonNumber = prisonNumber,
  content = notes!!,
  noteType = NoteType.GOAL,
  entityType = EntityType.GOAL,
  entityReference = reference,
  createdAtPrison = createdAtPrison,
  lastUpdatedAtPrison = lastUpdatedAtPrison,
)
