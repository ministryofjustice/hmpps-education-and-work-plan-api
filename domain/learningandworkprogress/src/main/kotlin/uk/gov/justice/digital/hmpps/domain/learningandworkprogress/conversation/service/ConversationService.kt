package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.Conversation
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.ConversationNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.CreateConversationDto
import java.util.UUID

private val log = KotlinLogging.logger {}

/**
 * Service class exposing methods that implement the business rules how applications must create and manage [Conversations]s.
 *
 * Applications using [Conversation]s must new up an instance of this class providing an implementation of
 * [ConversationPersistenceAdapter].
 *
 * This class is deliberately final so that it cannot be subclassed, ensuring that the business rules stay within the
 * domain. Service method behaviour can however be customized and extended by using the [ConversationEventService].
 *
 */
class ConversationService(
  private val persistenceAdapter: ConversationPersistenceAdapter,
  private val conversationEventService: ConversationEventService,
) {

  /**
   * Creates a new [Conversation] with the data in the specified [CreateConversationDto].
   */
  fun createConversation(createConversationDto: CreateConversationDto): Conversation {
    with(createConversationDto) {
      log.info { "Creating Conversation for prisoner [$prisonNumber]" }

      return persistenceAdapter.createConversation(createConversationDto)
        .also { conversationEventService.conversationCreated(it) }
    }
  }

  /**
   * Returns a list of conversations for the specified prisoner.
   * The order of the returned conversations is not guaranteed and it is up to the calling code to sort them
   * as necessary.
   */
  fun getPrisonerConversations(prisonNumber: String): List<Conversation> {
    log.info { "Retrieving Conversations for prisoner [$prisonNumber]" }
    return persistenceAdapter.getConversations(prisonNumber)
  }

  /**
   * Returns a [Conversation] identified by its `conversationReference`.
   * Throws [ConversationNotFoundException] if the [Conversation] cannot be found.
   */
  fun getConversation(conversationReference: UUID): Conversation {
    log.info { "Retrieving Conversation with reference [$conversationReference]" }
    return persistenceAdapter.getConversation(conversationReference)
      ?: throw ConversationNotFoundException(conversationReference).also {
        log.info { "Conversation with reference [$conversationReference] not found" }
      }
  }
}
