package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto

import java.util.UUID

/**
 * A DTO class that contains the data required to update a Conversation domain object.
 *
 * Currently, a [Conversation] has a single [ConversationNote] within it, and the only property within that that can be
 * updated is the note content.
 * As and when a [Conversation] contains multiple [ConversationNote]s or there are other properties that can be updated,
 * then this update DTO will likely need changing at that time. Until then this simplified DTO will suffice.
 */
data class UpdateConversationDto(
  val reference: UUID,
  val noteContent: String,
)
