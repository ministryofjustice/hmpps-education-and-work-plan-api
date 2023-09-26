package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.CIAG_INDUCTION_CREATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.CIAG_INDUCTION_UPDATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType.INDUCTION_CREATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType.INDUCTION_UPDATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service.TimelineService

@Service
class InboundEventsService(
  private val timelineService: TimelineService,
) {

  fun process(inboundEvent: InboundEvent) {
    val timelineEventType = when (inboundEvent.eventType()) {
      CIAG_INDUCTION_CREATED -> INDUCTION_CREATED
      CIAG_INDUCTION_UPDATED -> INDUCTION_UPDATED
    }
    timelineService.recordTimelineEvent(
      inboundEvent.prisonNumber(),
      buildTimelineEvent(inboundEvent, timelineEventType),
    )
  }

  private fun buildTimelineEvent(inboundEvent: InboundEvent, timelineEventType: TimelineEventType): TimelineEvent =
    with(inboundEvent) {
      TimelineEvent.newTimelineEvent(
        sourceReference = reference(),
        eventType = timelineEventType,
        prisonId = prisonId(),
        actionedBy = userId(),
        actionedByDisplayName = userDisplayName(),
        timestamp = occurredAt(),
      )
    }
}
