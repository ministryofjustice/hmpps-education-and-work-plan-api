package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
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
@Async
class AsyncGoalEventService(
  private val telemetryService: TelemetryService,
  private val timelineEventFactory: TimelineEventFactory,
  private val timelineService: TimelineService,
) : GoalEventService {

  override fun goalsCreated(prisonNumber: String, createdGoals: List<Goal>) {
    val correlationId = UUID.randomUUID()
    createdGoals.forEach { createdGoal ->
      log.debug { "Goal created event for prisoner [$prisonNumber]" }

      recordGoalCreatedTimelineEvent(
        prisonNumber = prisonNumber,
        createdGoal = createdGoal,
        correlationId = correlationId,
      )

      trackGoalCreatedTelemetryEvent(createdGoal = createdGoal)
    }
  }

  override fun goalUpdated(prisonNumber: String, previousGoal: Goal, updatedGoal: Goal) {
    log.debug { "Goal updated event for prisoner [$prisonNumber]" }

    recordGoalUpdatedTimelineEvents(
      prisonNumber = prisonNumber,
      previousGoal = previousGoal,
      updatedGoal = updatedGoal,
    )

    trackGoalUpdatedTelemetryEvents(previousGoal = previousGoal, updatedGoal = updatedGoal)
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
