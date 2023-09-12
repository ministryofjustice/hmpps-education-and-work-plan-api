package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.event

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Step
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType.GOAL_UPDATED
import java.util.UUID

/**
 * Responsible for identifying which [TimelineEvent]s have occurred, for example following an update to a [Goal].
 */
@Component
class TimelineEventResolver {

  /**
   * Determines what type of [TimelineEvent]s have occurred, following an update to a [Goal]. For example, whether its title
   * has changed, or one of its [Step]s has been completed.
   *
   * @param updatedGoal The newly updated [Goal] containing the latest changes.
   * @param existingGoal The state of the [Goal] just before it was updated.
   */
  fun resolveGoalUpdatedEvents(updatedGoal: Goal, existingGoal: Goal): List<TimelineEvent> {
    val events = mutableListOf<TimelineEvent>()
    // check if the goal itself was updated, excluding its steps and status
    if (hasGoalBeenUpdated(updatedGoal = updatedGoal, existingGoal = existingGoal)) {
      events.add(
        buildTimelineEvent(
          goal = updatedGoal,
          sourceReference = updatedGoal.reference,
          eventType = GOAL_UPDATED,
        ),
      )
    }

    if (hasGoalStatusChanged(updatedGoal, existingGoal)) {
      val eventType = resolveGoalStatusEventType(updatedGoal)
      events.add(
        buildTimelineEvent(
          goal = updatedGoal,
          sourceReference = updatedGoal.reference,
          eventType = eventType,
        ),
      )
    }

    // check if the status of any steps was changed
    updatedGoal.steps.forEach {
      val existingStep = getExistingStep(it, existingGoal.steps)
      if (hasStepStatusChanged(updatedStep = it, existingStep = existingStep)) {
        val eventType = resolveStepStatusEventType(it)
        events.add(
          buildTimelineEvent(
            goal = updatedGoal,
            sourceReference = existingStep!!.reference,
            eventType = eventType,
          ),
        )
      }
    }
    return events
  }

  private fun hasGoalBeenUpdated(updatedGoal: Goal, existingGoal: Goal) =
    updatedGoal.title != existingGoal.title ||
      updatedGoal.steps.size != existingGoal.steps.size ||
      updatedGoal.lastUpdatedAtPrison != existingGoal.lastUpdatedAtPrison ||
      updatedGoal.reviewDate != existingGoal.reviewDate ||
      updatedGoal.notes != existingGoal.notes

  private fun hasGoalStatusChanged(updatedGoal: Goal, existingGoal: Goal) =
    updatedGoal.status != existingGoal.status

  private fun hasStepStatusChanged(updatedStep: Step, existingStep: Step?) =
    existingStep != null && existingStep.status != updatedStep.status

  private fun getExistingStep(updatedStep: Step, existingSteps: List<Step>): Step? =
    existingSteps.firstOrNull { it.reference == updatedStep.reference }

  private fun resolveGoalStatusEventType(updatedGoal: Goal): TimelineEventType {
    val eventType = when (updatedGoal.status) {
      GoalStatus.COMPLETED -> TimelineEventType.GOAL_COMPLETED
      GoalStatus.ACTIVE -> TimelineEventType.GOAL_STARTED
      GoalStatus.ARCHIVED -> TimelineEventType.GOAL_ARCHIVED
    }
    return eventType
  }

  private fun resolveStepStatusEventType(it: Step): TimelineEventType {
    val eventType = when (it.status) {
      StepStatus.NOT_STARTED -> TimelineEventType.STEP_NOT_STARTED
      StepStatus.ACTIVE -> TimelineEventType.STEP_STARTED
      StepStatus.COMPLETE -> TimelineEventType.STEP_COMPLETED
    }
    return eventType
  }

  private fun buildTimelineEvent(goal: Goal, sourceReference: UUID, eventType: TimelineEventType) =
    TimelineEvent.newTimelineEvent(
      sourceReference = sourceReference.toString(),
      eventType = eventType,
      prisonId = goal.lastUpdatedAtPrison,
      createdBy = goal.lastUpdatedBy!!,
      createdByDisplayName = goal.lastUpdatedByDisplayName!!,
    )
}
