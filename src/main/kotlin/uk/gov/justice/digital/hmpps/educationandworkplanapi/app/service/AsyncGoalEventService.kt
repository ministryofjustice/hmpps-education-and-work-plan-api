package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.GoalEventService
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService
import java.util.UUID

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

  override suspend fun goalsCreated(prisonNumber: String, createdGoals: List<Goal>) {
    val correlationId = UUID.randomUUID()
    createdGoals.forEach { createdGoal ->
      coroutineScope {
        log.debug { "Goal created event for prisoner [$prisonNumber]" }
        async {
          recordGoalCreatedTimelineEvent(
            prisonNumber = prisonNumber,
            createdGoal = createdGoal,
            correlationId = correlationId,
          )
        }
        async { trackGoalCreatedTelemetryEvent(createdGoal = createdGoal) }
      }
    }
  }

  override suspend fun goalUpdated(prisonNumber: String, previousGoal: Goal, updatedGoal: Goal) {
    coroutineScope {
      log.debug { "Goal updated event for prisoner [$prisonNumber]" }

      async {
        recordGoalUpdatedTimelineEvents(
          prisonNumber = prisonNumber,
          previousGoal = previousGoal,
          updatedGoal = updatedGoal,
        )
      }
      async { trackGoalUpdatedTelemetryEvents(previousGoal = previousGoal, updatedGoal = updatedGoal) }
    }
  }

  private fun recordGoalCreatedTimelineEvent(prisonNumber: String, createdGoal: Goal, correlationId: UUID = UUID.randomUUID()) {
    timelineService.recordTimelineEvent(
      prisonNumber,
      timelineEventFactory.goalCreatedTimelineEvent(createdGoal, correlationId),
    )
  }

  private fun recordGoalUpdatedTimelineEvents(prisonNumber: String, previousGoal: Goal, updatedGoal: Goal) {
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
