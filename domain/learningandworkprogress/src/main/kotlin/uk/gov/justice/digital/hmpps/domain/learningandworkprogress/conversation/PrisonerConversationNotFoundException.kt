package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation

import java.util.UUID

/**
 * Thrown when a specified Conversation for a prisoner cannot be found.
 */
class PrisonerConversationNotFoundException(val conversationReference: UUID, val prisonNumber: String) :
  RuntimeException("Conversation with reference [$conversationReference] for prisoner [$prisonNumber] not found")
