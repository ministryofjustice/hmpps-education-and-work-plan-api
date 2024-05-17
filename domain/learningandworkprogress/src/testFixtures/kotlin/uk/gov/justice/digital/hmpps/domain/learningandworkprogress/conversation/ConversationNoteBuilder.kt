package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation

import java.time.Instant
import java.util.UUID

fun aValidConversationNote(
  reference: UUID = UUID.randomUUID(),
  content: String = "Chris engaged well during our meeting and has made some good progress",
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  lastUpdatedBy: String? = "bjones_gen",
  lastUpdatedByDisplayName: String? = "Barry Jones",
  lastUpdatedAt: Instant? = Instant.now(),
  lastUpdatedAtPrison: String = "BXI",
): ConversationNote =
  ConversationNote(
    reference = reference,
    content = content,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedByDisplayName = lastUpdatedByDisplayName,
    lastUpdatedAt = lastUpdatedAt,
    lastUpdatedAtPrison = lastUpdatedAtPrison,
  )
