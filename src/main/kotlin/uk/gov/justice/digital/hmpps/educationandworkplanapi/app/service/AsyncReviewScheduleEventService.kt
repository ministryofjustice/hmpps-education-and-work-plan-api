package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.UpdatedReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleEventService
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.REVIEW_SCHEDULE_DEADLINE_NEW
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.REVIEW_SCHEDULE_DEADLINE_OLD
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.REVIEW_SCHEDULE_EXEMPTION_REASON
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.REVIEW_SCHEDULE_STATUS_NEW
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.REVIEW_SCHEDULE_STATUS_OLD
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventPublisher

private val log = KotlinLogging.logger {}

/**
 * Implementation of [ReviewScheduleEventService] for performing additional asynchronous actions related to Review schedule events.
 */
@Component
@Async
class AsyncReviewScheduleEventService(
  private val timelineService: TimelineService,
  private val telemetryService: TelemetryService,
  private val eventPublisher: EventPublisher,
) : ReviewScheduleEventService {

  override fun reviewScheduleStatusUpdated(updatedReviewScheduleStatus: UpdatedReviewScheduleStatus) {
    log.debug { "Review schedule status updated event for prisoner [${updatedReviewScheduleStatus.prisonNumber}]" }
    timelineService.recordTimelineEvent(
      updatedReviewScheduleStatus.prisonNumber,
      buildReviewScheduleStatusUpdatedEvent(updatedReviewScheduleStatus),
    )
    telemetryService.trackReviewScheduleStatusUpdated(updatedReviewScheduleStatus = updatedReviewScheduleStatus)
    eventPublisher.createAndPublishReviewScheduleEvent(updatedReviewScheduleStatus.prisonNumber)
  }

  private fun buildReviewScheduleStatusUpdatedEvent(updatedReviewScheduleStatus: UpdatedReviewScheduleStatus): TimelineEvent =
    with(updatedReviewScheduleStatus) {
      TimelineEvent.newTimelineEvent(
        sourceReference = reference.toString(),
        eventType = TimelineEventType.ACTION_PLAN_REVIEW_SCHEDULE_STATUS_UPDATED,
        prisonId = updatedAtPrison,
        actionedBy = updatedBy,
        timestamp = updatedAt,
        contextualInfo = mapOf(
          REVIEW_SCHEDULE_STATUS_OLD to oldStatus.name,
          REVIEW_SCHEDULE_STATUS_NEW to newStatus.name,
          REVIEW_SCHEDULE_DEADLINE_OLD to oldReviewDate.toString(),
          REVIEW_SCHEDULE_DEADLINE_NEW to newReviewDate.toString(),
          *exemptionReason
            ?.let {
              arrayOf(REVIEW_SCHEDULE_EXEMPTION_REASON to it)
            } ?: arrayOf(),
        ),
      )
    }
}
