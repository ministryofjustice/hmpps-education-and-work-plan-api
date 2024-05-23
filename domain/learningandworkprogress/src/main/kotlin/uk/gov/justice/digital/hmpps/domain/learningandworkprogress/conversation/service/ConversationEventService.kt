package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.Conversation

/**
 * Interface for defining [Conversation] related lifecycle events.
 */
interface ConversationEventService {

  /**
   * Implementations providing custom code for when a [Conversation] is created.
   */
  fun conversationCreated(conversation: Conversation)

  /**
   * Implementations providing custom code for when a [Conversation] is updated.
   */
  fun conversationUpdated(conversation: Conversation)
}
