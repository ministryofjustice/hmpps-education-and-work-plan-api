package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation

import java.time.Instant
import java.util.UUID

/**
 * Represents notes taken at a single Learning And Work Progress [Conversation] held with a prisoner.
 */
data class ConversationNote(
  val reference: UUID,
  val content: String,
  val createdBy: String,
  val createdAt: Instant,
  val createdAtPrison: String,
  val lastUpdatedBy: String,
  val lastUpdatedAt: Instant,
  val lastUpdatedAtPrison: String,
)
