package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity

import java.time.Instant
import java.util.UUID

fun aValidTimelineEventEntity(
  id: UUID = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  sourceReference: String = UUID.randomUUID().toString(),
  eventType: TimelineEventType = TimelineEventType.GOAL_CREATED,
  contextualInfo: String? = null,
  prisonId: String = "BXI",
  actionedBy: String = "asmith_gen",
  actionedByDisplayName: String? = "Alex Smith",
  timestamp: Instant = Instant.now(),
  createdAt: Instant = Instant.now(),
  correlationId: UUID? = UUID.randomUUID(),
) = TimelineEventEntity(
  id = id,
  reference = reference,
  sourceReference = sourceReference,
  eventType = eventType,
  contextualInfo = contextualInfo,
  prisonId = prisonId,
  actionedBy = actionedBy,
  actionedByDisplayName = actionedByDisplayName,
  timestamp = timestamp,
  createdAt = createdAt,
  correlationId = correlationId,
)
