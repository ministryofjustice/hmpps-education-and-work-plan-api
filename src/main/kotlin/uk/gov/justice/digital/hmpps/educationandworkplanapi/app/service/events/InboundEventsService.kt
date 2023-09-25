package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.events

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType.INDUCTION_CREATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType.INDUCTION_UPDATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service.TimelineService

@Service
class InboundEventsService(
  private val timelineService: TimelineService,
) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun process(inboundEvent: InboundEvent) {
    when (inboundEvent) {
      is CiagInductionCreatedEvent ->
        timelineService.recordTimelineEvent(
          inboundEvent.prisonNumber(),
          buildCiagTimelineEvent(inboundEvent, INDUCTION_CREATED),
        )

      is CiagInductionUpdatedEvent ->
        timelineService.recordTimelineEvent(
          inboundEvent.prisonNumber(),
          buildCiagTimelineEvent(inboundEvent, INDUCTION_UPDATED),
        )

      else -> log.warn("Unsupported event ${inboundEvent.javaClass.name}")
    }
  }

  private fun buildCiagTimelineEvent(inboundEvent: InboundEvent, timelineEventType: TimelineEventType): TimelineEvent =
    with(inboundEvent as CiagInductionEvent) {
      TimelineEvent.newTimelineEvent(
        sourceReference = reference(),
        eventType = timelineEventType,
        prisonId = prisonId(),
        actionedBy = userId(),
        actionedByDisplayName = userDisplayName(),
      )
    }
}
