package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline

import java.time.Instant
import java.util.UUID

fun aValidTimeline(
  prisonNumber: String = "A1234AB",
  events: List<TimelineEvent> = listOf(aValidTimelineEvent()),
) = Timeline(
  prisonNumber = prisonNumber,
  events = events,
)

fun aValidTimelineEvent(
  reference: UUID = UUID.randomUUID(),
  sourceReference: String = UUID.randomUUID().toString(),
  eventType: TimelineEventType = TimelineEventType.GOAL_CREATED,
  contextualInfo: String? = null,
  prisonId: String = "BXI",
  createdBy: String = "asmith_gen",
  createdByDisplayName: String = "Alex Smith",
  timestamp: Instant = Instant.now(),
) = TimelineEvent(
  reference = reference,
  sourceReference = sourceReference,
  eventType = eventType,
  contextualInfo = contextualInfo,
  prisonId = prisonId,
  createdBy = createdBy,
  createdByDisplayName = createdByDisplayName,
  timestamp = timestamp,
)
