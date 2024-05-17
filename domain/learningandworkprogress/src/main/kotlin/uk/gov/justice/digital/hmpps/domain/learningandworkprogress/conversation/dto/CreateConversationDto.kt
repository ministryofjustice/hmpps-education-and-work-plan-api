package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.ConversationType

/**
 * A DTO class that contains the data required to create a new Conversation domain object
 */
data class CreateConversationDto(
  val prisonNumber: String,
  val type: ConversationType,
  val note: CreateConversationNoteDto,
)
