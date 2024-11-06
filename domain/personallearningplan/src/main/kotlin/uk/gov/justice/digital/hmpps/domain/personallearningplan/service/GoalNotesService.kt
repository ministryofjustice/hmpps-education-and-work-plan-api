package uk.gov.justice.digital.hmpps.domain.personallearningplan.service

import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import java.util.UUID

interface GoalNotesService {

  fun createNotes(prisonNumber: String, createdGoals: List<Goal>)

  fun getNotes(entityReference: UUID): String?

  fun deleteNote(entityReference: UUID)

  fun updateNotes(entityReference: UUID, lastUpdatedAtPrison: String, updatedText: String)
}
