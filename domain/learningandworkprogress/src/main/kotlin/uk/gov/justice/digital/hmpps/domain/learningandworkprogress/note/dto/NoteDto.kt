package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto

import java.time.Instant
import java.util.UUID

data class NoteDto(
  val reference: UUID,
  val content: String,
  val createdBy: String?,
  val createdAt: Instant?,
  val createdAtPrison: String,
  val lastUpdatedBy: String?,
  val lastUpdatedAt: Instant?,
  val lastUpdatedAtPrison: String,
  val noteType: NoteType,
  val entityType: EntityType,
  val entityReference: UUID,
)

enum class EntityType {
  GOAL,
  INDUCTION,
  REVIEW,
}

enum class NoteType(val entityType: EntityType) {
  GOAL(EntityType.GOAL),
  GOAL_ARCHIVAL(EntityType.GOAL),
  GOAL_COMPLETION(EntityType.GOAL),
  REVIEW(EntityType.REVIEW),
  INDUCTION(EntityType.INDUCTION),
}
