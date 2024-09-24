package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto

import java.util.UUID

data class CreateNoteDto(
  val prisonNumber: String,
  val entityReference: UUID,
  val entityType: EntityType,
  val noteType: NoteType,
  val content: String,
  val createdAtPrison: String,
  val lastUpdatedAtPrison: String,
)
