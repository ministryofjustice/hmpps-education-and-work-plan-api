package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalEventService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service.TimelineService

private val log = KotlinLogging.logger {}

/**
 * Implementation of [GoalEventService] for performing additional asynchronous actions related to [Goal] events.
 */
@Component
class AsyncGoalEventService(
  private val telemetryService: TelemetryService,
  private val timelineEventFactory: TimelineEventFactory,
  private val timelineService: TimelineService,
) : GoalEventService {

  override fun goalCreated(prisonNumber: String, createdGoal: Goal) {
    runBlocking {
      log.debug { "Goal created event for prisoner [$prisonNumber]" }
      val timelineEvent = timelineEventFactory.goalCreatedTimelineEvent(createdGoal)
      launch { timelineService.recordTimelineEvent(prisonNumber, timelineEvent) }
      launch { telemetryService.trackGoalCreateEvent(createdGoal) }
    }
  }

  override fun goalUpdated(prisonNumber: String, previousGoal: Goal, updatedGoal: Goal) {
    runBlocking {
      log.debug { "Goal updated event for prisoner [$prisonNumber]" }
      val timelineEvents = timelineEventFactory.goalUpdatedEvents(previousGoal = previousGoal, updatedGoal = updatedGoal)
      launch { timelineService.recordTimelineEvents(prisonNumber, timelineEvents) }
      launch { telemetryService.trackGoalUpdateEvent(updatedGoal) }
    }
  }
}
