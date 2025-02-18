package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline

import java.time.Instant
import java.util.UUID

fun aValidTimelineEventEntity(
  id: UUID = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  sourceReference: String = UUID.randomUUID().toString(),
  eventType: TimelineEventType = TimelineEventType.GOAL_CREATED,
  contextualInfo: Map<TimelineEventContext, String> = emptyMap(),
  prisonId: String = "BXI",
  actionedBy: String = "asmith_gen",
  timestamp: Instant = Instant.now(),
  createdAt: Instant = Instant.now(),
  correlationId: UUID = UUID.randomUUID(),
) = TimelineEventEntity(
  reference = reference,
  sourceReference = sourceReference,
  eventType = eventType,
  contextualInfo = contextualInfo,
  prisonId = prisonId,
  actionedBy = actionedBy,
  timestamp = timestamp,
  correlationId = correlationId,
).apply {
  this.id = id
  this.createdAt = createdAt
}
