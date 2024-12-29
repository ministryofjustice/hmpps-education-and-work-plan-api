package uk.gov.justice.digital.hmpps.domain.timeline

import java.time.Instant
import java.util.UUID

fun aValidTimeline(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234AB",
  events: List<TimelineEvent> = listOf(aValidTimelineEvent()),
) = Timeline(
  reference = reference,
  prisonNumber = prisonNumber,
  events = events,
)

fun aValidTimelineEvent(
  reference: UUID = UUID.randomUUID(),
  sourceReference: String = UUID.randomUUID().toString(),
  eventType: TimelineEventType = TimelineEventType.GOAL_CREATED,
  contextualInfo: Map<TimelineEventContext, String> = emptyMap(),
  prisonId: String = "BXI",
  actionedBy: String = "asmith_gen",
  timestamp: Instant = Instant.now(),
  correlationId: UUID = UUID.randomUUID(),
) = TimelineEvent(
  reference = reference,
  sourceReference = sourceReference,
  eventType = eventType,
  contextualInfo = contextualInfo,
  prisonId = prisonId,
  actionedBy = actionedBy,
  timestamp = timestamp,
  correlationId = correlationId,
)

fun aValidPrisonMovementTimelineEvent(
  reference: UUID = UUID.randomUUID(),
  sourceReference: String = "1",
  eventType: TimelineEventType = TimelineEventType.PRISON_ADMISSION,
  contextualInfo: Map<TimelineEventContext, String> = emptyMap(),
  prisonId: String = "BXI",
  actionedBy: String = "system",
  timestamp: Instant = Instant.now(),
  correlationId: UUID = UUID.randomUUID(),
) = TimelineEvent(
  reference = reference,
  sourceReference = sourceReference,
  eventType = eventType,
  contextualInfo = contextualInfo,
  prisonId = prisonId,
  actionedBy = actionedBy,
  timestamp = timestamp,
  correlationId = correlationId,
)
