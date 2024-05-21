package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import java.time.Instant
import java.util.UUID

fun aValidConversationEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = aValidPrisonNumber(),
  conversationNote: ConversationNoteEntity = aValidConversationNoteEntity(),
  type: ConversationType = ConversationType.REVIEW,
  createdAt: Instant? = Instant.now(),
  createdBy: String? = "asmith_gen",
  updatedAt: Instant? = Instant.now(),
  updatedBy: String? = "bjones_gen",
): ConversationEntity =
  ConversationEntity(
    id = id,
    reference = reference,
    prisonNumber = prisonNumber,
    note = conversationNote,
    type = type,
    createdAt = createdAt,
    createdBy = createdBy,
    updatedAt = updatedAt,
    updatedBy = updatedBy,
  )
