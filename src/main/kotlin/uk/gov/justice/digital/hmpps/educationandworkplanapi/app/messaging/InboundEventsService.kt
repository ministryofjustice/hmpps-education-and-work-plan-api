package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType.INDUCTION_CREATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType.INDUCTION_UPDATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service.TimelineService

private val log = KotlinLogging.logger {}

@Service
class InboundEventsService(
  private val timelineService: TimelineService,
) {

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
