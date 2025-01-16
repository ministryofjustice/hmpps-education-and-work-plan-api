package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation

import java.time.Instant
import java.util.UUID

fun aValidConversationNote(
  reference: UUID = UUID.randomUUID(),
  content: String = "Chris engaged well during our meeting and has made some good progress",
  createdBy: String = "asmith_gen",
  createdAt: Instant = Instant.now(),
  createdAtPrison: String = "BXI",
  lastUpdatedBy: String = "bjones_gen",
  lastUpdatedAt: Instant = Instant.now(),
  lastUpdatedAtPrison: String = "BXI",
): ConversationNote =
  ConversationNote(
    reference = reference,
    content = content,
    createdBy = createdBy,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedAt = lastUpdatedAt,
    lastUpdatedAtPrison = lastUpdatedAtPrison,
  )
