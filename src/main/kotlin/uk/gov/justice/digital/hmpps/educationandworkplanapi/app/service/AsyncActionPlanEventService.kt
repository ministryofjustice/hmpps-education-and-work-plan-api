package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlan
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.ActionPlanEventService
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService

private val log = KotlinLogging.logger {}

/**
 * Implementation of [ActionPlanEventService] for performing additional asynchronous actions related to [ActionPlan]
 * events.
 */
@Component
@Async
class AsyncActionPlanEventService(
  private val telemetryService: TelemetryService,
  private val timelineEventFactory: TimelineEventFactory,
  private val timelineService: TimelineService,
) : ActionPlanEventService {
  override fun actionPlanCreated(actionPlan: ActionPlan) {
    log.info { "ActionPlan created event for prisoner [${actionPlan.prisonNumber}]" }

    actionPlan.goals.forEach {
      telemetryService.trackGoalCreatedEvent(it)
    }

    val timelineEvents = timelineEventFactory.actionPlanCreatedEvent(actionPlan)
    timelineService.recordTimelineEvents(actionPlan.prisonNumber, timelineEvents)
  }
}
