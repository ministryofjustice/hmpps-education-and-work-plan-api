package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Step
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType
import java.util.UUID
/**
 * Responsible for identifying which [TimelineEvent]s have occurred, for example following an update to a [Goal].
 */
@Component
class TimelineEventFactory {

  fun actionPlanCreatedEvent(actionPlan: ActionPlan) =
    buildTimelineEvent(
      actionPlan.goals[0],
      actionPlan.reference,
      TimelineEventType.ACTION_PLAN_CREATED,
      contextualInfo = null,
    )

  fun goalCreatedTimelineEvent(goal: Goal) =
    buildTimelineEvent(goal, goal.reference, TimelineEventType.GOAL_CREATED, contextualInfo = goal.title)

  /**
   * Determines what type of [TimelineEvent]s have occurred, following an update to a [Goal]. For example, whether its title
   * has changed, or one of its [Step]s has been completed.
   *
   * @param previousGoal The state of the [Goal] just before it was updated.
   * @param updatedGoal The newly updated [Goal] containing the latest changes.
   */
  fun goalUpdatedEvents(previousGoal: Goal, updatedGoal: Goal): List<TimelineEvent> {
    val events = mutableListOf<TimelineEvent>()
    // check if the goal itself was updated, excluding its steps and status
    if (hasGoalBeenUpdated(previousGoal = previousGoal, updatedGoal = updatedGoal)) {
      events.add(
        buildTimelineEvent(
          goal = updatedGoal,
          sourceReference = updatedGoal.reference,
          eventType = TimelineEventType.GOAL_UPDATED,
          contextualInfo = updatedGoal.title,
        ),
      )
    }

    if (hasGoalStatusChanged(previousGoal = previousGoal, updatedGoal = updatedGoal)) {
      val eventType = getGoalStatusEventType(updatedGoal)
      events.add(
        buildTimelineEvent(
          goal = updatedGoal,
          sourceReference = updatedGoal.reference,
          eventType = eventType,
          contextualInfo = updatedGoal.title,
        ),
      )
    }

    // check if any steps have been changed
    updatedGoal.steps.forEach {
      val previousStep = getPreviousStep(previousGoal.steps, it)
      if (hasStepBeenUpdated(previousStep = previousStep, updatedStep = it)) {
        events.add(
          buildTimelineEvent(
            goal = updatedGoal,
            sourceReference = previousStep!!.reference,
            eventType = TimelineEventType.STEP_UPDATED,
            contextualInfo = it.title,
          ),
        )
      }
      if (hasStepStatusChanged(previousStep = previousStep, updatedStep = it)) {
        val eventType = getStepStatusEventType(it)
        events.add(
          buildTimelineEvent(
            goal = updatedGoal,
            sourceReference = previousStep!!.reference,
            eventType = eventType,
            contextualInfo = it.title,
          ),
        )
      }
    }
    return events
  }

  private fun hasGoalBeenUpdated(previousGoal: Goal, updatedGoal: Goal) =
    updatedGoal.title != previousGoal.title ||
      updatedGoal.steps.size != previousGoal.steps.size ||
      updatedGoal.lastUpdatedAtPrison != previousGoal.lastUpdatedAtPrison ||
      updatedGoal.targetCompletionDate != previousGoal.targetCompletionDate ||
      updatedGoal.notes != previousGoal.notes

  private fun hasGoalStatusChanged(previousGoal: Goal, updatedGoal: Goal) =
    updatedGoal.status != previousGoal.status

  private fun hasStepBeenUpdated(previousStep: Step?, updatedStep: Step) =
    previousStep != null && (
      updatedStep.title != previousStep.title ||
        updatedStep.targetDateRange != previousStep.targetDateRange ||
        updatedStep.sequenceNumber != previousStep.sequenceNumber
      )

  private fun hasStepStatusChanged(previousStep: Step?, updatedStep: Step) =
    previousStep != null && previousStep.status != updatedStep.status

  private fun getPreviousStep(previousSteps: List<Step>, updatedStep: Step): Step? =
    previousSteps.firstOrNull { it.reference == updatedStep.reference }

  private fun getGoalStatusEventType(updatedGoal: Goal): TimelineEventType {
    val eventType = when (updatedGoal.status) {
      GoalStatus.COMPLETED -> TimelineEventType.GOAL_COMPLETED
      GoalStatus.ACTIVE -> TimelineEventType.GOAL_STARTED
      GoalStatus.ARCHIVED -> TimelineEventType.GOAL_ARCHIVED
    }
    return eventType
  }

  private fun getStepStatusEventType(step: Step): TimelineEventType {
    val eventType = when (step.status) {
      StepStatus.NOT_STARTED -> TimelineEventType.STEP_NOT_STARTED
      StepStatus.ACTIVE -> TimelineEventType.STEP_STARTED
      StepStatus.COMPLETE -> TimelineEventType.STEP_COMPLETED
    }
    return eventType
  }

  private fun buildTimelineEvent(
    goal: Goal,
    sourceReference: UUID,
    eventType: TimelineEventType,
    contextualInfo: String?,
  ) =
    TimelineEvent.newTimelineEvent(
      sourceReference = sourceReference.toString(),
      eventType = eventType,
      // we can use the lastUpdatedBy fields for create action plan/create goal events, since it will be the same as the actionedBy fields initially
      prisonId = goal.lastUpdatedAtPrison,
      actionedBy = goal.lastUpdatedBy!!,
      actionedByDisplayName = goal.lastUpdatedByDisplayName!!,
      contextualInfo = contextualInfo,
    )
}
