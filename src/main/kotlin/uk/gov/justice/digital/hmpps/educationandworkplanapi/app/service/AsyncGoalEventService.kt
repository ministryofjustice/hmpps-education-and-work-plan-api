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
      launch { recordCreateGoalTimelineEvent(prisonNumber = prisonNumber, createdGoal = createdGoal) }
      launch { trackGoalCreatedTelemetryEvent(createdGoal = createdGoal) }
    }
  }

  override fun goalUpdated(prisonNumber: String, previousGoal: Goal, updatedGoal: Goal) {
    runBlocking {
      log.debug { "Goal updated event for prisoner [$prisonNumber]" }

      launch {
        recordUpdateGoalTimelineEvents(
          prisonNumber = prisonNumber,
          previousGoal = previousGoal,
          updatedGoal = updatedGoal,
        )
      }
      launch { trackGoalUpdatedTelemetryEvents(previousGoal = previousGoal, updatedGoal = updatedGoal) }
    }
  }

  private fun recordCreateGoalTimelineEvent(prisonNumber: String, createdGoal: Goal) {
    timelineService.recordTimelineEvent(
      prisonNumber,
      timelineEventFactory.goalCreatedTimelineEvent(createdGoal),
    )
  }

  private fun recordUpdateGoalTimelineEvents(prisonNumber: String, previousGoal: Goal, updatedGoal: Goal) {
    timelineService.recordTimelineEvents(
      prisonNumber,
      timelineEventFactory.goalUpdatedEvents(previousGoal = previousGoal, updatedGoal = updatedGoal),
    )
  }

  private fun trackGoalCreatedTelemetryEvent(createdGoal: Goal) {
    telemetryService.trackGoalCreatedEvent(createdGoal)
  }

  private fun trackGoalUpdatedTelemetryEvents(previousGoal: Goal, updatedGoal: Goal) {
    telemetryService.trackGoalUpdatedEvents(previousGoal = previousGoal, updatedGoal = updatedGoal)
  }
}
