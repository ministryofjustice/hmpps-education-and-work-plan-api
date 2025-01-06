package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionPersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.ActionPlanPersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventPublisher
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.CreateInitialReviewScheduleMapper

private val log = KotlinLogging.logger {}

@Service
class ReviewScheduleAdapter(
  private val inductionPersistenceAdapter: InductionPersistenceAdapter,
  private val actionPlanPersistenceAdapter: ActionPlanPersistenceAdapter,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val createInitialReviewScheduleMapper: CreateInitialReviewScheduleMapper,
  private val reviewScheduleService: ReviewScheduleService,
  private val eventPublisher: EventPublisher,
  private val telemetryService: TelemetryService,
  private val timelineService: TimelineService,
) {

  fun createInitialReviewScheduleIfInductionAndActionPlanExists(prisonNumber: String): ReviewSchedule? {
    // validate
    if (
      inductionPersistenceAdapter.getInduction(prisonNumber) == null ||
      actionPlanPersistenceAdapter.getActionPlan(prisonNumber) == null
    ) {
      return null
    }

    // Create initial review schedule
    val prisoner = prisonerSearchApiService.getPrisoner(prisonNumber)
    val reviewScheduleDto = createInitialReviewScheduleMapper.fromPrisonerToDomain(
      prisoner = prisoner,
      isReadmission = false,
      isTransfer = false,
    )
    val reviewSchedule = reviewScheduleService.createInitialReviewSchedule(reviewScheduleDto)
    if (reviewSchedule != null) {
      followOnEvents(prisonNumber, reviewSchedule)
    }
    return reviewSchedule
  }

  private fun followOnEvents(prisonNumber: String, reviewSchedule: ReviewSchedule) {
    log.debug { "Review schedule created for prisoner [$prisonNumber]" }
    timelineService.recordTimelineEvent(
      prisonNumber,
      buildReviewScheduleCreatedEvent(reviewSchedule),
    )
    telemetryService.trackReviewScheduleStatusCreated(reviewSchedule = reviewSchedule)
    eventPublisher.createAndPublishReviewScheduleEvent(prisonNumber)
  }

  private fun buildReviewScheduleCreatedEvent(reviewSchedule: ReviewSchedule): TimelineEvent =
    with(reviewSchedule) {
      TimelineEvent.newTimelineEvent(
        sourceReference = reference.toString(),
        eventType = TimelineEventType.ACTION_PLAN_REVIEW_SCHEDULE_CREATED,
        prisonId = createdAtPrison,
        actionedBy = createdBy,
        timestamp = createdAt,
        contextualInfo = mapOf(
          TimelineEventContext.REVIEW_SCHEDULE_STATUS_NEW to scheduleStatus.name,
          TimelineEventContext.REVIEW_SCHEDULE_DEADLINE_NEW to reviewScheduleWindow.dateTo.toString(),
        ),
      )
    }
}
