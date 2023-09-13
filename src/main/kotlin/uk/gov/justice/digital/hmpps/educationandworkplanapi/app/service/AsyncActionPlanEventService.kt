package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.ActionPlanEventService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service.TimelineService

private val log = KotlinLogging.logger {}

/**
 * Implementation of [ActionPlanEventService] for performing additional asynchronous actions related to [ActionPlan]
 * events.
 */
@Component
class AsyncActionPlanEventService(
  private val timelineEventFactory: TimelineEventFactory,
  private val timelineService: TimelineService,
) : ActionPlanEventService {
  override fun actionPlanCreated(actionPlan: ActionPlan) {
    runBlocking {
      log.info { "ActionPlan created event for prisoner [${actionPlan.prisonNumber}]" }
      val timelineEvent = timelineEventFactory.actionPlanCreatedEvent(actionPlan)
      launch { timelineService.recordTimelineEvent(actionPlan.prisonNumber, timelineEvent) }
    }
  }
}
