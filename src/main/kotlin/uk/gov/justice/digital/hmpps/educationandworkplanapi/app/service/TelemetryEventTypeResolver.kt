package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.GOAL_ARCHIVED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.GOAL_COMPLETED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.GOAL_STARTED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.GOAL_UPDATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.STEP_ADDED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.STEP_COMPLETED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.STEP_NOT_STARTED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.STEP_REMOVED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.STEP_STARTED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.STEP_UPDATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Step
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus

@Component
class TelemetryEventTypeResolver {

  /**
   * Compares two [Goal]s and returns a list of [GoalTelemetryEventType] reflecting the types of updates
   * between the two [Goal]s.
   *
   * Depending on the type and number of updates between the two [Goal]s it is possible for the returned list to contain
   * duplicates. For example if 2 steps were deleted there will be 2 [STEP_REMOVED]s in the returned list.
   * It is the responsibility of the consumer of the returned list to de-dupe it if necessary/relevant.
   *
   * This method is deliberately marked as `internal` as there are no envisaged use cases for it outside the domain
   * module.
   */
  internal fun resolveUpdateEventTypes(previousGoal: Goal, updatedGoal: Goal): List<GoalTelemetryEventType> {
    val updateTypes = mutableListOf<GoalTelemetryEventType>()
    // check if the goal itself was updated, excluding its steps and status
    if (hasGoalBeenUpdated(previousGoal = previousGoal, updatedGoal = updatedGoal)) {
      updateTypes.add(GOAL_UPDATED)
    }

    // check if the goal's status was updated
    if (hasGoalStatusChanged(previousGoal = previousGoal, updatedGoal = updatedGoal)) {
      val updateTypeForGoalStatusChange = getTelemetryUpdateEventTypeForGoalStatusChange(updatedGoal)
      updateTypes.add(updateTypeForGoalStatusChange)
    }

    val previousStepReferences = previousGoal.steps.map { it.reference }
    val updatedStepReferences = updatedGoal.steps.map { it.reference }
    // check for any deleted steps (steps that are in previousGoal but not in updatedGoal)
    previousStepReferences.forEach {
      if (!updatedStepReferences.contains(it)) {
        updateTypes.add(STEP_REMOVED)
      }
    }
    // check for any added steps (steps that are in updatedGoal but not in previousGoal)
    updatedStepReferences.forEach {
      if (!previousStepReferences.contains(it)) {
        updateTypes.add(STEP_ADDED)
      }
    }

    // check for any updates to the steps, including their status
    previousGoal.steps.forEach { previousStep ->
      if (updatedStepReferences.contains(previousStep.reference)) {
        val updatedStep = updatedGoal.steps.first { previousStep.reference == it.reference }

        // check for any updates to steps, excluding status
        if (hasStepBeenUpdated(previousStep, updatedStep)) {
          updateTypes.add(STEP_UPDATED)
        }
        // check for any updates to step status
        if (hasStepStatusChanged(previousStep, updatedStep)) {
          val updateTypeForStepStatusChange = getTelemetryUpdateEventTypeForStepStatusChange(updatedStep)
          updateTypes.add(updateTypeForStepStatusChange)
        }
      }
    }

    return updateTypes.toList()
  }

  private fun hasGoalBeenUpdated(previousGoal: Goal, updatedGoal: Goal) =
    updatedGoal.title != previousGoal.title ||
      updatedGoal.lastUpdatedAtPrison != previousGoal.lastUpdatedAtPrison ||
      updatedGoal.targetCompletionDate != previousGoal.targetCompletionDate ||
      updatedGoal.notes != previousGoal.notes

  private fun hasGoalStatusChanged(previousGoal: Goal, updatedGoal: Goal) =
    updatedGoal.status != previousGoal.status

  private fun getTelemetryUpdateEventTypeForGoalStatusChange(updatedGoal: Goal): GoalTelemetryEventType =
    when (updatedGoal.status) {
      GoalStatus.COMPLETED -> GOAL_COMPLETED
      GoalStatus.ACTIVE -> GOAL_STARTED
      GoalStatus.ARCHIVED -> GOAL_ARCHIVED
    }

  private fun hasStepBeenUpdated(previousStep: Step, updatedStep: Step) =
    updatedStep.title != previousStep.title ||
      updatedStep.sequenceNumber != previousStep.sequenceNumber

  private fun hasStepStatusChanged(previousStep: Step, updatedStep: Step) =
    previousStep.status != updatedStep.status

  private fun getTelemetryUpdateEventTypeForStepStatusChange(step: Step): GoalTelemetryEventType =
    when (step.status) {
      StepStatus.NOT_STARTED -> STEP_NOT_STARTED
      StepStatus.ACTIVE -> STEP_STARTED
      StepStatus.COMPLETE -> STEP_COMPLETED
    }
}
