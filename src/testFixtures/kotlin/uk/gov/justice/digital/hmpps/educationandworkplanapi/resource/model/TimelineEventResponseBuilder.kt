package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import java.time.OffsetDateTime
import java.util.UUID

fun aValidTimelineEventResponse(
  reference: UUID = UUID.randomUUID(),
  sourceReference: String = UUID.randomUUID().toString(),
  eventType: TimelineEventType = TimelineEventType.GOAL_CREATED,
  prisonId: String = "BXI",
  actionedBy: String = "asmith_gen",
  actionedByDisplayName: String = "Alex Smith",
  timestamp: OffsetDateTime = OffsetDateTime.now(),
  contextualInfo: String? = "Learn French",
  correlationId: UUID = UUID.randomUUID(),
): TimelineEventResponse =
  TimelineEventResponse(
    reference = reference,
    sourceReference = sourceReference,
    eventType = eventType,
    prisonId = prisonId,
    actionedBy = actionedBy,
    actionedByDisplayName = actionedByDisplayName,
    timestamp = timestamp,
    contextualInfo = contextualInfo,
    correlationId = correlationId,
  )
