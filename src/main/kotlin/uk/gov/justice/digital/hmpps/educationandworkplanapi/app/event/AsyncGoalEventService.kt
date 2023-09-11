package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.event

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.TelemetryService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Step
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalEventService

private val log = KotlinLogging.logger {}

/**
 * Implementation of [GoalEventService] for performing additional asynchronous actions related to [Goal] events.
 */
@Component
class AsyncGoalEventService(private val telemetryService: TelemetryService) : GoalEventService {

  override fun goalCreated(prisonNumber: String, createdGoal: Goal) {
    runBlocking {
      log.debug { "Goal created event for prisoner [$prisonNumber]" }
      launch {
        telemetryService.trackGoalCreateEvent(createdGoal)
      }
    }
  }

  override fun goalUpdated(prisonNumber: String, updatedGoal: Goal, existingGoal: Goal) {
    runBlocking {
      log.debug { "Goal updated event for prisoner [$prisonNumber]" }
      // val events = resolveUpdateEvents(updatedGoal, existingGoal)
      launch {
        telemetryService.trackGoalUpdateEvent(updatedGoal)
      }
    }
  }

  private fun resolveUpdateEvents(updatedGoal: Goal, existingGoal: Goal): List<GoalEvent> {
    val events = mutableListOf<GoalEvent>()
    // check if the goal itself was updated, excluding its steps and status
    if (goalHasBeenUpdated(updatedGoal, existingGoal)) {
      events.add(GoalEvent(reference = updatedGoal.reference, eventType = EventType.GOAL_UPDATED))
    }

    // check if goal's status was updated
    if (goalStatusChanged(updatedGoal, existingGoal)) {
      val eventType = when (updatedGoal.status) {
        GoalStatus.COMPLETED -> EventType.GOAL_COMPLETED
        GoalStatus.ACTIVE -> EventType.GOAL_STARTED
        GoalStatus.ARCHIVED -> EventType.GOAL_ARCHIVED
      }
      events.add(GoalEvent(reference = updatedGoal.reference, eventType = eventType))
    }

    // check if the status of the steps were changed
    updatedGoal.steps.forEach {
      val existingStep = getExistingStep(it, existingGoal.steps)
      if (stepStatusChanged(existingStep, it)) {
        val eventType = when (it.status) {
          StepStatus.NOT_STARTED -> EventType.STEP_NOT_STARTED
          StepStatus.ACTIVE -> EventType.STEP_STARTED
          StepStatus.COMPLETE -> EventType.STEP_COMPLETED
        }
        events.add(GoalEvent(reference = updatedGoal.reference, eventType = eventType))
      }
    }
    return events
  }

  private fun stepStatusChanged(existingStep: Step?, it: Step) =
    existingStep != null && existingStep.status != it.status

  private fun goalStatusChanged(updatedGoal: Goal, existingGoal: Goal) =
    updatedGoal.status != existingGoal.status

  private fun goalHasBeenUpdated(updatedGoal: Goal, existingGoal: Goal) =
    updatedGoal.title != existingGoal.title ||
      updatedGoal.steps.size != existingGoal.steps.size ||
      updatedGoal.lastUpdatedAtPrison != existingGoal.lastUpdatedAtPrison ||
      updatedGoal.reviewDate != existingGoal.reviewDate ||
      updatedGoal.notes != existingGoal.notes

  private fun getExistingStep(updatedStep: Step, existingSteps: List<Step>): Step? =
    existingSteps.firstOrNull { it.reference == updatedStep.reference }
}
