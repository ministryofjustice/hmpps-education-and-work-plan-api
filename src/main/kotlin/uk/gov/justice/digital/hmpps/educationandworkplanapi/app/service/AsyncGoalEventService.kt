package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalEventService

/**
 * Implementation of [GoalEventService] that decorates Goal Service methods with additional behaviours that are
 * invoked asynchronously.
 */
@Component
class AsyncGoalEventService(private val telemetryService: TelemetryService) : GoalEventService {

  override fun goalCreated(createdGoal: Goal) {
    runBlocking {
      launch {
        telemetryService.trackGoalCreateEvent(createdGoal)
      }
    }
  }

  override fun goalUpdated(updatedGoal: Goal) {
    runBlocking {
      launch {
        telemetryService.trackGoalUpdateEvent(updatedGoal)
      }
    }
  }
}
