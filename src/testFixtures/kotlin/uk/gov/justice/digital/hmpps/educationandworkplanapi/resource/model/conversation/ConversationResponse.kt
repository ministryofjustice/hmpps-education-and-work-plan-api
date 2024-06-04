package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.conversation

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ConversationResponse
import java.time.OffsetDateTime
import java.util.UUID

fun aValidConversationResponse(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234BC",
  note: String = "Pay close attention to Peter's behaviour.",
  createdBy: String = "asmith_gen",
  createdByDisplayName: String = "Alex Smith",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "asmith_gen",
  updatedByDisplayName: String = "Alex Smith",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedAtPrison: String = "BXI",
): ConversationResponse = ConversationResponse(
  reference = reference,
  prisonNumber = prisonNumber,
  note = note,
  createdBy = createdBy,
  createdByDisplayName = createdByDisplayName,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  updatedBy = updatedBy,
  updatedByDisplayName = updatedByDisplayName,
  updatedAt = updatedAt,
  updatedAtPrison = updatedAtPrison,
)
