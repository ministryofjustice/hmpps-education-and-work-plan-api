package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.UpdatedInductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.UpdatedReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlan
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Step
import uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus.ACTIVE
import uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus.COMPLETE
import uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.GOAL_ARCHIVED_REASON
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.GOAL_ARCHIVED_REASON_OTHER
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.GOAL_TITLE
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.INDUCTION_SCHEDULE_DEADLINE_DATE
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.INDUCTION_SCHEDULE_DEADLINE_NEW
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.INDUCTION_SCHEDULE_DEADLINE_OLD
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.INDUCTION_SCHEDULE_EXEMPTION_REASON
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.INDUCTION_SCHEDULE_STATUS
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.INDUCTION_SCHEDULE_STATUS_NEW
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.INDUCTION_SCHEDULE_STATUS_OLD
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.REVIEW_SCHEDULE_DEADLINE_NEW
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.REVIEW_SCHEDULE_DEADLINE_OLD
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.REVIEW_SCHEDULE_EXEMPTION_REASON
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.REVIEW_SCHEDULE_STATUS_NEW
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.REVIEW_SCHEDULE_STATUS_OLD
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.STEP_TITLE
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType.ACTION_PLAN_REVIEW_SCHEDULE_CREATED
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType.ACTION_PLAN_REVIEW_SCHEDULE_STATUS_UPDATED
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType.GOAL_ARCHIVED
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType.GOAL_COMPLETED
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType.GOAL_UNARCHIVED
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType.GOAL_UPDATED
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType.INDUCTION_SCHEDULE_CREATED
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType.INDUCTION_SCHEDULE_STATUS_UPDATED
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType.STEP_COMPLETED
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType.STEP_NOT_STARTED
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType.STEP_STARTED
import java.util.UUID

/**
 * Responsible for identifying which [TimelineEvent]s have occurred, for example following an update to a [Goal].
 */
@Component
class TimelineEventFactory(private val userService: ManageUserService) {

  fun actionPlanCreatedEvent(actionPlan: ActionPlan): List<TimelineEvent> {
    val events = mutableListOf<TimelineEvent>()

    val correlationId = UUID.randomUUID()
    events.add(
      buildTimelineEvent(
        actionPlan.goals[0],
        actionPlan.reference,
        TimelineEventType.ACTION_PLAN_CREATED,
        contextualInfo = emptyMap(),
        correlationId = correlationId,
      ),
    )
    actionPlan.goals.forEach {
      events.add(goalCreatedTimelineEvent(it, correlationId))
    }
    return events
  }

  fun goalCreatedTimelineEvent(goal: Goal, correlationId: UUID = UUID.randomUUID()) = buildTimelineEvent(
    goal = goal,
    sourceReference = goal.reference,
    eventType = TimelineEventType.GOAL_CREATED,
    contextualInfo = mapOf(GOAL_TITLE to goal.title),
    correlationId = correlationId,
  )

  /**
   * Determines what type of [TimelineEvent]s have occurred, following an update to a [Goal]. For example, whether its title
   * has changed, or one of its [Step]s has been completed.
   *
   * Note that if a Goal's Step has been modified, but not the Goal itself, we record a Step changed Timeline event
   * with some details about the Step, however we also record a Goal changed event for the parent Goal. In reality,
   * changes to Steps may turn out to be more granular/detailed than we need, but the information is persisted in case
   * we ever need it.
   *
   * @param previousGoal The state of the [Goal] just before it was updated.
   * @param updatedGoal The newly updated [Goal] containing the latest changes.
   */
  fun goalUpdatedEvents(previousGoal: Goal, updatedGoal: Goal): List<TimelineEvent> {
    val timelineEvents = mutableListOf<TimelineEvent>()
    val correlationId = UUID.randomUUID()

    if (hasGoalBeenUpdated(previousGoal = previousGoal, updatedGoal = updatedGoal)) {
      timelineEvents.add(
        buildTimelineEvent(
          goal = updatedGoal,
          sourceReference = updatedGoal.reference,
          eventType = GOAL_UPDATED,
          contextualInfo = mapOf(GOAL_TITLE to updatedGoal.title),
          correlationId = correlationId,
        ),
      )
    }

    // check if any steps have been changed
    val stepEvents = getStepUpdatedEvents(updatedGoal, previousGoal, correlationId)

    // if one or more Steps have changed, but the Goal itself hasn't been modified, then record an overall GOAL_UPDATED event
    if (timelineEvents.isEmpty() && stepEvents.isNotEmpty()) {
      timelineEvents.add(
        buildTimelineEvent(
          goal = updatedGoal,
          sourceReference = updatedGoal.reference,
          eventType = GOAL_UPDATED,
          contextualInfo = mapOf(GOAL_TITLE to updatedGoal.title),
          correlationId = correlationId,
        ),
      )
    }
    // if applicable, add the Step related events to the Timeline
    if (stepEvents.isNotEmpty()) {
      timelineEvents.addAll(stepEvents)
    }

    return timelineEvents
  }

  fun goalArchivedTimelineEvent(goal: Goal, correlationId: UUID = UUID.randomUUID()) = buildTimelineEvent(
    goal = goal,
    sourceReference = goal.reference,
    eventType = GOAL_ARCHIVED,
    contextualInfo = listOfNotNull(
      GOAL_TITLE to goal.title,
      GOAL_ARCHIVED_REASON to goal.archiveReason!!.toString(),
      goal.archiveReasonOther?.let { GOAL_ARCHIVED_REASON_OTHER to it },
    ).toMap(),
    correlationId = correlationId,
  )

  fun goalCompletedTimelineEvent(goal: Goal, correlationId: UUID = UUID.randomUUID()) = buildTimelineEvent(
    goal = goal,
    sourceReference = goal.reference,
    eventType = GOAL_COMPLETED,
    contextualInfo = mapOf(GOAL_TITLE to goal.title),
    correlationId = correlationId,
  )

  fun goalUnArchivedTimelineEvent(goal: Goal, correlationId: UUID = UUID.randomUUID()) = buildTimelineEvent(
    goal = goal,
    sourceReference = goal.reference,
    eventType = GOAL_UNARCHIVED,
    contextualInfo = mapOf(GOAL_TITLE to goal.title),
    correlationId = correlationId,
  )

  private fun hasGoalBeenUpdated(previousGoal: Goal, updatedGoal: Goal) = updatedGoal.title != previousGoal.title ||
    updatedGoal.steps.size != previousGoal.steps.size ||
    updatedGoal.lastUpdatedAtPrison != previousGoal.lastUpdatedAtPrison ||
    updatedGoal.targetCompletionDate != previousGoal.targetCompletionDate ||
    updatedGoal.notes != previousGoal.notes

  fun getStepUpdatedEvents(
    updatedGoal: Goal,
    previousGoal: Goal,
    correlationId: UUID,
  ): MutableList<TimelineEvent> {
    val stepEvents = mutableListOf<TimelineEvent>()
    updatedGoal.steps.forEach {
      val previousStep = getPreviousStep(previousGoal.steps, it)
      if (hasStepStatusChanged(previousStep = previousStep, updatedStep = it)) {
        stepEvents.add(
          buildTimelineEvent(
            goal = updatedGoal,
            sourceReference = previousStep!!.reference,
            eventType = getStepStatusEventType(it),
            contextualInfo = mapOf(STEP_TITLE to it.title),
            correlationId = correlationId,
          ),
        )
      }
      if (hasStepBeenUpdated(previousStep = previousStep, updatedStep = it)) {
        stepEvents.add(
          buildTimelineEvent(
            goal = updatedGoal,
            sourceReference = previousStep!!.reference,
            eventType = TimelineEventType.STEP_UPDATED,
            contextualInfo = mapOf(STEP_TITLE to it.title),
            correlationId = correlationId,
          ),
        )
      }
    }
    return stepEvents
  }

  private fun hasStepBeenUpdated(previousStep: Step?, updatedStep: Step) = previousStep != null &&
    (
      updatedStep.title != previousStep.title ||
        updatedStep.sequenceNumber != previousStep.sequenceNumber
      )

  private fun hasStepStatusChanged(previousStep: Step?, updatedStep: Step) = previousStep != null && previousStep.status != updatedStep.status

  private fun getPreviousStep(previousSteps: List<Step>, updatedStep: Step): Step? = previousSteps.firstOrNull { it.reference == updatedStep.reference }

  private fun getStepStatusEventType(step: Step): TimelineEventType {
    val eventType = when (step.status) {
      NOT_STARTED -> STEP_NOT_STARTED
      ACTIVE -> STEP_STARTED
      COMPLETE -> STEP_COMPLETED
    }
    return eventType
  }

  private fun buildTimelineEvent(
    goal: Goal,
    sourceReference: UUID,
    eventType: TimelineEventType,
    contextualInfo: Map<TimelineEventContext, String>,
    correlationId: UUID = UUID.randomUUID(),
  ) = TimelineEvent.newTimelineEvent(
    sourceReference = sourceReference.toString(),
    eventType = eventType,
    // we can use the lastUpdatedBy fields for create action plan/create goal events, since it will be the same as the actionedBy fields initially
    prisonId = goal.lastUpdatedAtPrison,
    actionedBy = goal.lastUpdatedBy!!,
    contextualInfo = contextualInfo,
    correlationId = correlationId,
  )

  fun inductionScheduleCreatedTimelineEvent(
    inductionSchedule: InductionSchedule,
    correlationId: UUID = UUID.randomUUID(),
  ) = with(inductionSchedule) {
    TimelineEvent.newTimelineEvent(
      sourceReference = reference.toString(),
      eventType = INDUCTION_SCHEDULE_CREATED,
      prisonId = "N/A",
      actionedBy = lastUpdatedBy,
      contextualInfo = mapOf(
        INDUCTION_SCHEDULE_STATUS to scheduleStatus.name,
        INDUCTION_SCHEDULE_DEADLINE_DATE to deadlineDate.toString(),
      ),
      correlationId = correlationId,
    )
  }

  fun inductionScheduleStatusUpdatedEvent(updatedInductionScheduleStatus: UpdatedInductionScheduleStatus): TimelineEvent = with(updatedInductionScheduleStatus) {
    TimelineEvent.newTimelineEvent(
      sourceReference = reference.toString(),
      eventType = INDUCTION_SCHEDULE_STATUS_UPDATED,
      actionedBy = updatedBy,
      timestamp = updatedAt,
      prisonId = updatedAtPrison,
      contextualInfo = mapOf(
        INDUCTION_SCHEDULE_STATUS_OLD to oldStatus.name,
        INDUCTION_SCHEDULE_STATUS_NEW to newStatus.name,
        INDUCTION_SCHEDULE_DEADLINE_OLD to oldDeadlineDate.toString(),
        INDUCTION_SCHEDULE_DEADLINE_NEW to newDeadlineDate.toString(),
        *exemptionReason
          ?.let {
            arrayOf(INDUCTION_SCHEDULE_EXEMPTION_REASON to it)
          } ?: arrayOf(),
      ),
    )
  }

  fun reviewScheduleCreatedTimelineEvent(
    reviewSchedule: ReviewSchedule,
    correlationId: UUID = UUID.randomUUID(),
  ) = with(reviewSchedule) {
    TimelineEvent.newTimelineEvent(
      sourceReference = reference.toString(),
      eventType = ACTION_PLAN_REVIEW_SCHEDULE_CREATED,
      prisonId = createdAtPrison,
      actionedBy = createdBy,
      timestamp = createdAt,
      contextualInfo = mapOf(
        REVIEW_SCHEDULE_STATUS_NEW to scheduleStatus.name,
        REVIEW_SCHEDULE_DEADLINE_NEW to reviewScheduleWindow.dateTo.toString(),
      ),
    )
  }

  fun reviewScheduleStatusUpdatedTimelineEvent(updatedReviewScheduleStatus: UpdatedReviewScheduleStatus): TimelineEvent = with(updatedReviewScheduleStatus) {
    TimelineEvent.newTimelineEvent(
      sourceReference = reference.toString(),
      eventType = ACTION_PLAN_REVIEW_SCHEDULE_STATUS_UPDATED,
      prisonId = updatedAtPrison,
      actionedBy = updatedBy,
      timestamp = updatedAt,
      contextualInfo = mapOf(
        REVIEW_SCHEDULE_STATUS_OLD to oldStatus.name,
        REVIEW_SCHEDULE_STATUS_NEW to newStatus.name,
        REVIEW_SCHEDULE_DEADLINE_OLD to oldReviewDate.toString(),
        REVIEW_SCHEDULE_DEADLINE_NEW to newReviewDate.toString(),
        *exemptionReason
          ?.let {
            arrayOf(REVIEW_SCHEDULE_EXEMPTION_REASON to it)
          } ?: arrayOf(),
      ),
    )
  }
}
