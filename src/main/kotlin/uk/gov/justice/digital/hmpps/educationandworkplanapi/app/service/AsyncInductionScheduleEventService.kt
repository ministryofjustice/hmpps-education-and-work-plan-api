package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.UpdatedInductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleEventService
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventPublisher

private val log = KotlinLogging.logger {}

/**
 * Implementation of [InductionScheduleEventService] for performing additional asynchronous actions related to Induction schedule events.
 */
@Component
@Async
class AsyncInductionScheduleEventService(
  private val timelineEventFactory: TimelineEventFactory,
  private val timelineService: TimelineService,
  private val telemetryService: TelemetryService,
  private val eventPublisher: EventPublisher,
) : InductionScheduleEventService {

  override fun inductionScheduleCreated(inductionSchedule: InductionSchedule) =
    with(inductionSchedule) {
      log.debug { "Induction schedule created event for prisoner [$prisonNumber]" }

      timelineService.recordTimelineEvent(
        prisonNumber,
        timelineEventFactory.inductionScheduleCreatedTimelineEvent(this),
      )
      telemetryService.trackInductionScheduleCreated(this)
      eventPublisher.createAndPublishInductionEvent(prisonNumber)
    }

  override fun inductionScheduleStatusUpdated(updatedInductionScheduleStatus: UpdatedInductionScheduleStatus) =
    with(updatedInductionScheduleStatus) {
      log.debug { "Induction schedule status updated event for prisoner [$prisonNumber]" }

      timelineService.recordTimelineEvent(
        prisonNumber,
        timelineEventFactory.inductionScheduleStatusUpdatedEvent(this),
      )
      telemetryService.trackInductionScheduleStatusUpdated(this)
      eventPublisher.createAndPublishInductionEvent(prisonNumber)
    }
}
