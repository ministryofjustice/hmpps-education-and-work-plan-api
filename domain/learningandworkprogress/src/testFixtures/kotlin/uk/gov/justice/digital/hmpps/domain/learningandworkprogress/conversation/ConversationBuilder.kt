package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation

import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import java.util.UUID

fun aValidConversation(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = aValidPrisonNumber(),
  type: ConversationType = ConversationType.REVIEW,
  note: ConversationNote = aValidConversationNote(),
): Conversation =
  Conversation(
    reference = reference,
    prisonNumber = prisonNumber,
    type = type,
    note = note,
  )
