package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto

import java.util.UUID

data class UpdateNoteDto(
  val reference: UUID,
  val content: String,
  val lastUpdatedAtPrison: String,
)
