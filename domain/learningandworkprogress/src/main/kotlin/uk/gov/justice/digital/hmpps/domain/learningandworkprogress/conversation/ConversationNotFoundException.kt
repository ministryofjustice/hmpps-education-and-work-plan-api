package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation

import java.util.UUID

/**
 * Thrown when a specified Conversation cannot be found.
 */
class ConversationNotFoundException(val conversationReference: UUID) :
  RuntimeException("Conversation with reference [$conversationReference] not found")
