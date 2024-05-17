package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto

/**
 * A DTO class that contains the data required to create a new Conversation Note domain object
 */
data class CreateConversationNoteDto(
  val prisonId: String,
  val content: String,
)
