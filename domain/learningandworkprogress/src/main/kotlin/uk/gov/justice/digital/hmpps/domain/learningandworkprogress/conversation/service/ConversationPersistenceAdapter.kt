package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.Conversation
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.CreateConversationDto
import java.util.UUID

/**
 * Persistence Adapter for [Conversation] instances.
 *
 * Implementations should use the underlying persistence of the application in question, eg: JPA, Mongo, Dynamo, Redis etc
 *
 * Implementations should not throw exceptions. These are not part of the interface and are not checked or handled by
 * [ConversationService].
 */
interface ConversationPersistenceAdapter {

  /**
   * Creates a new [Conversation] and returns persisted instance.
   */
  fun createConversation(createConversationDto: CreateConversationDto): Conversation

  /**
   * Returns a [List] of [Conversation]s for the prisoner. An empty list is returned if there are no Conversations
   * for the prisoner.
   */
  fun getConversations(prisonNumber: String): List<Conversation>

  /**
   * Returns a prisoner [Conversation] identified by its reference, or null if not found.
   */
  fun getConversation(conversationReference: UUID): Conversation?
}
