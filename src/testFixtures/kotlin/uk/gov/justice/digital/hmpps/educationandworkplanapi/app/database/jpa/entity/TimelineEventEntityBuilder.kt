package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity

import java.time.Instant
import java.util.UUID

fun aValidTimelineEventEntity(
  id: UUID = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  sourceReference: String = UUID.randomUUID().toString(),
  eventType: TimelineEventType = TimelineEventType.GOAL_CREATED,
  contextualInfo: String? = null,
  createdAtPrison: String = "BXI",
  createdBy: String = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  timestamp: Instant = Instant.now(),
  createdAt: Instant = Instant.now(),
) = TimelineEventEntity(
  id = id,
  reference = reference,
  sourceReference = sourceReference,
  eventType = eventType,
  contextualInfo = contextualInfo,
  createdAtPrison = createdAtPrison,
  createdBy = createdBy,
  createdByDisplayName = createdByDisplayName,
  timestamp = timestamp,
  createdAt = createdAt,
)
