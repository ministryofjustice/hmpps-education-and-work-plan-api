package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.event

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.ActionPlanEventService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service.TimelineService

private val log = KotlinLogging.logger {}

/**
 * Implementation of [ActionPlanEventService] for performing additional asynchronous actions related to [ActionPlan]
 * events.
 */
@Component
class AsyncActionPlanEventService(private val timelineService: TimelineService) : ActionPlanEventService {
  // TODO - RR-314 - add unit test
  override fun actionPlanCreated(actionPlan: ActionPlan) {
    runBlocking {
      log.info { "ActionPlan created event for prisoner [${actionPlan.prisonNumber}]" }
      launch {
        timelineService.recordTimelineEvent(actionPlan.prisonNumber, actionPlanCreatedEvent(actionPlan))
      }
    }
  }

  private fun actionPlanCreatedEvent(actionPlan: ActionPlan) =
    TimelineEvent.newTimelineEvent(
      sourceReference = actionPlan.reference.toString(),
      eventType = TimelineEventType.ACTION_PLAN_CREATED,
      prisonId = actionPlan.goals[0].createdAtPrison,
      createdBy = actionPlan.goals[0].createdBy!!,
      createdByDisplayName = actionPlan.goals[0].createdByDisplayName!!,
    )
}
