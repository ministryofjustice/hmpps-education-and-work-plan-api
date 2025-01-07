package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.UpdatedReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleEventService
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventPublisher

private val log = KotlinLogging.logger {}

/**
 * Implementation of [ReviewScheduleEventService] for performing additional asynchronous actions related to Review schedule events.
 */
@Component
@Async
class AsyncReviewScheduleEventService(
  private val timelineEventFactory: TimelineEventFactory,
  private val timelineService: TimelineService,
  private val telemetryService: TelemetryService,
  private val eventPublisher: EventPublisher,
) : ReviewScheduleEventService {

  override fun reviewScheduleCreated(reviewSchedule: ReviewSchedule) =
    with(reviewSchedule) {
      log.debug { "Review Schedule created event for prisoner [$prisonNumber]" }

      timelineService.recordTimelineEvent(
        prisonNumber,
        timelineEventFactory.reviewScheduleCreatedTimelineEvent(this),
      )
      telemetryService.trackReviewScheduleCreated(this)
      eventPublisher.createAndPublishReviewScheduleEvent(prisonNumber)
    }

  override fun reviewScheduleStatusUpdated(updatedReviewScheduleStatus: UpdatedReviewScheduleStatus) =
    with(updatedReviewScheduleStatus) {
      log.debug { "Review schedule status updated event for prisoner [$prisonNumber]" }

      timelineService.recordTimelineEvent(
        prisonNumber,
        timelineEventFactory.reviewScheduleStatusUpdatedTimelineEvent(this),
      )
      telemetryService.trackReviewScheduleStatusUpdated(this)
      eventPublisher.createAndPublishReviewScheduleEvent(prisonNumber)
    }
}
