package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.CompletedReview
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewEventService
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY_ROLE
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.COMPLETED_REVIEW_CONDUCTED_IN_PERSON_DATE
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.COMPLETED_REVIEW_ENTERED_ONLINE_AT
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.COMPLETED_REVIEW_ENTERED_ONLINE_BY
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.COMPLETED_REVIEW_NOTES
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService

private val log = KotlinLogging.logger {}

/**
 * Implementation of [ReviewEventService] for performing additional asynchronous actions related to Review events.
 */
@Component
@Async
class AsyncReviewEventService(
  private val timelineService: TimelineService,
  private val telemetryService: TelemetryService,
  private val userService: ManageUserService,
) : ReviewEventService {

  override fun reviewCompleted(completedReview: CompletedReview) {
    log.debug { "Review completed event for prisoner [${completedReview.prisonNumber}]" }
    timelineService.recordTimelineEvent(completedReview.prisonNumber, buildReviewCompletedEvent(completedReview))
    telemetryService.trackReviewCompleted(completedReview = completedReview)
  }

  private fun buildReviewCompletedEvent(completedReview: CompletedReview): TimelineEvent = with(completedReview) {
    TimelineEvent.newTimelineEvent(
      sourceReference = reference.toString(),
      eventType = TimelineEventType.ACTION_PLAN_REVIEW_COMPLETED,
      prisonId = createdAtPrison,
      actionedBy = createdBy,
      timestamp = createdAt,
      contextualInfo = mapOf(
        COMPLETED_REVIEW_ENTERED_ONLINE_AT to createdAt.toString(),
        COMPLETED_REVIEW_ENTERED_ONLINE_BY to userService.getUserDetails(createdBy).name,
        COMPLETED_REVIEW_CONDUCTED_IN_PERSON_DATE to completedDate.toString(),
        COMPLETED_REVIEW_NOTES to note.content,
        *conductedBy
          ?.let {
            arrayOf(
              COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY to it.name,
              COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY_ROLE to it.role,
            )
          } ?: arrayOf(),
      ),
    )
  }
}
