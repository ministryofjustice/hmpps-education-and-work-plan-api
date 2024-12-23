package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.UpdatedInductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleEventService
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventPublisher

private val log = KotlinLogging.logger {}

/**
 * Implementation of [InductionScheduleEventService] for performing additional asynchronous actions related to Induction schedule events.
 */
@Component
@Async
class AsyncInductionScheduleEventService(
  private val timelineService: TimelineService,
  private val telemetryService: TelemetryService,
  private val eventPublisher: EventPublisher,
) : InductionScheduleEventService {

  override fun inductionScheduleStatusUpdated(updatedInductionScheduleStatus: UpdatedInductionScheduleStatus) {
    log.debug { "Induction schedule status updated event for prisoner [${updatedInductionScheduleStatus.prisonNumber}]" }
    timelineService.recordTimelineEvent(
      updatedInductionScheduleStatus.prisonNumber,
      buildInductionScheduleStatusUpdatedEvent(updatedInductionScheduleStatus),
    )
    telemetryService.trackInductionScheduleStatusUpdated(updatedInductionScheduleStatus = updatedInductionScheduleStatus)
    eventPublisher.createAndPublishInductionEvent(updatedInductionScheduleStatus.prisonNumber)
  }

  private fun buildInductionScheduleStatusUpdatedEvent(updatedInductionScheduleStatus: UpdatedInductionScheduleStatus): TimelineEvent =
    with(updatedInductionScheduleStatus) {
      TimelineEvent.newTimelineEvent(
        sourceReference = reference.toString(),
        eventType = TimelineEventType.INDUCTION_SCHEDULE_STATUS_UPDATED,
        actionedBy = updatedBy,
        timestamp = updatedAt,
        prisonId = "N/A",
        contextualInfo = mapOf(
          TimelineEventContext.INDUCTION_SCHEDULE_STATUS_OLD to oldStatus.name,
          TimelineEventContext.INDUCTION_SCHEDULE_STATUS_NEW to newStatus.name,
          TimelineEventContext.INDUCTION_SCHEDULE_DEADLINE_OLD to oldDeadlineDate.toString(),
          TimelineEventContext.INDUCTION_SCHEDULE_DEADLINE_NEW to newDeadlineDate.toString(),
          *exemptionReason
            ?.let {
              arrayOf(TimelineEventContext.INDUCTION_SCHEDULE_EXEMPTION_REASON to it)
            } ?: arrayOf(),
        ),
      )
    }
}
