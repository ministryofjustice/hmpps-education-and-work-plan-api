package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline

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
  contextualInfo: String? = null,
  prisonId: String = "BXI",
  actionedBy: String = "asmith_gen",
  actionedByDisplayName: String = "Alex Smith",
  timestamp: Instant = Instant.now(),
) = TimelineEvent(
  reference = reference,
  sourceReference = sourceReference,
  eventType = eventType,
  contextualInfo = contextualInfo,
  prisonId = prisonId,
  actionedBy = actionedBy,
  actionedByDisplayName = actionedByDisplayName,
  timestamp = timestamp,
)
