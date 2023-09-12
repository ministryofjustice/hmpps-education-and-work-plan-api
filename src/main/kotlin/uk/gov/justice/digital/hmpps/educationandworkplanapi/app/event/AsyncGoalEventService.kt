package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.event

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.TelemetryService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalEventService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent.Companion.newTimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service.TimelineService

private val log = KotlinLogging.logger {}

/**
 * Implementation of [GoalEventService] for performing additional asynchronous actions related to [Goal] events.
 */
@Component
class AsyncGoalEventService(
  private val telemetryService: TelemetryService,
  private val timelineEventResolver: TimelineEventResolver,
  private val timelineService: TimelineService,
) : GoalEventService {

  override fun goalCreated(prisonNumber: String, createdGoal: Goal) {
    runBlocking {
      log.debug { "Goal created event for prisoner [$prisonNumber]" }
      launch {
        telemetryService.trackGoalCreateEvent(createdGoal)
        timelineService.recordTimelineEvent(prisonNumber, goalCreatedTimelineEvent(createdGoal))
      }
    }
  }

  override fun goalUpdated(prisonNumber: String, updatedGoal: Goal, existingGoal: Goal) {
    runBlocking {
      log.debug { "Goal updated event for prisoner [$prisonNumber]" }
      val timelineEvents = timelineEventResolver.resolveGoalUpdatedEvents(updatedGoal, existingGoal)
      launch {
        telemetryService.trackGoalUpdateEvent(updatedGoal)
        timelineService.recordTimelineEvents(prisonNumber, timelineEvents)
      }
    }
  }

  private fun goalCreatedTimelineEvent(goal: Goal) =
    newTimelineEvent(
      sourceReference = goal.reference.toString(),
      eventType = TimelineEventType.GOAL_CREATED,
      prisonId = goal.createdAtPrison,
      createdBy = goal.createdBy!!,
      createdByDisplayName = goal.createdByDisplayName!!,
    )
}
