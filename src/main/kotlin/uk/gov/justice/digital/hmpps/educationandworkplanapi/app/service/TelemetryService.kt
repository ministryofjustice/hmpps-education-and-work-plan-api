package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.Conversation
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Induction
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.trackEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ConversationTelemetryEventType.CONVERSATION_CREATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ConversationTelemetryEventType.CONVERSATION_UPDATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.GOAL_ARCHIVED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.GOAL_COMPLETED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.GOAL_CREATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.GOAL_UNARCHIVED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.GOAL_UPDATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.STEP_REMOVED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.InductionTelemetryEventType.INDUCTION_CREATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.InductionTelemetryEventType.INDUCTION_UPDATED
import java.util.UUID

/**
 * Service class exposing methods to log telemetry events to ApplicationInsights.
 */
@Service
class TelemetryService(
  private val telemetryClient: TelemetryClient,
  private val telemetryEventTypeResolver: TelemetryEventTypeResolver,
) {

  fun trackGoalCreatedEvent(createdGoal: Goal, correlationId: UUID = UUID.randomUUID()) {
    sendTelemetryEventForGoal(
      goal = createdGoal,
      correlationId = correlationId,
      telemetryEventType = GOAL_CREATED,
    )
  }

  fun trackGoalUpdatedEvent(updatedGoal: Goal, correlationId: UUID = UUID.randomUUID()) {
    sendTelemetryEventForGoal(
      goal = updatedGoal,
      correlationId = correlationId,
      telemetryEventType = GOAL_UPDATED,
    )
  }

  fun trackStepRemovedEvent(updatedGoal: Goal, correlationId: UUID = UUID.randomUUID()) {
    sendTelemetryEventForGoal(
      goal = updatedGoal,
      correlationId = correlationId,
      telemetryEventType = STEP_REMOVED,
    )
  }

  fun trackGoalArchivedEvent(archivedGoal: Goal, correlationId: UUID = UUID.randomUUID()) {
    sendTelemetryEventForGoal(
      goal = archivedGoal,
      correlationId = correlationId,
      telemetryEventType = GOAL_ARCHIVED,
    )
  }

  fun trackGoalCompletedEvent(goal: Goal, correlationId: UUID = UUID.randomUUID()) {
    sendTelemetryEventForGoal(
      goal = goal,
      correlationId = correlationId,
      telemetryEventType = GOAL_COMPLETED,
    )
  }

  fun trackGoalUnArchivedEvent(unArchivedGoal: Goal, correlationId: UUID = UUID.randomUUID()) {
    sendTelemetryEventForGoal(
      goal = unArchivedGoal,
      correlationId = correlationId,
      telemetryEventType = GOAL_UNARCHIVED,
    )
  }

  fun trackInductionCreated(induction: Induction) {
    sendTelemetryEventForInduction(
      induction = induction,
      telemetryEventType = INDUCTION_CREATED,
    )
  }

  fun trackInductionUpdated(induction: Induction) {
    sendTelemetryEventForInduction(
      induction = induction,
      telemetryEventType = INDUCTION_UPDATED,
    )
  }

  fun trackConversationCreated(conversation: Conversation) {
    sendTelemetryEventForConversation(
      conversation = conversation,
      telemetryEventType = CONVERSATION_CREATED,
    )
  }

  fun trackConversationUpdated(conversation: Conversation) {
    sendTelemetryEventForConversation(
      conversation = conversation,
      telemetryEventType = CONVERSATION_UPDATED,
    )
  }

  /**
   * Sends all goal update telemetry tracking events based on the differences between the previousGoal and the
   * updatedGoal.
   */
  fun trackGoalUpdatedEvents(previousGoal: Goal, updatedGoal: Goal) {
    val correlationId = UUID.randomUUID()
    val telemetryUpdateEvents =
      telemetryEventTypeResolver.resolveUpdateEventTypes(previousGoal = previousGoal, updatedGoal = updatedGoal)
    telemetryUpdateEvents.forEach {
      when (it) {
        GOAL_UPDATED -> trackGoalUpdatedEvent(updatedGoal, correlationId)
        STEP_REMOVED -> trackStepRemovedEvent(updatedGoal, correlationId)
        else -> {}
      }
    }
  }

  private fun sendTelemetryEventForGoal(goal: Goal, correlationId: UUID, telemetryEventType: GoalTelemetryEventType) =
    telemetryClient.trackEvent(telemetryEventType.value, telemetryEventType.customDimensions(goal, correlationId))

  private fun sendTelemetryEventForInduction(induction: Induction, telemetryEventType: InductionTelemetryEventType) =
    telemetryClient.trackEvent(telemetryEventType.value, telemetryEventType.customDimensions(induction))

  private fun sendTelemetryEventForConversation(conversation: Conversation, telemetryEventType: ConversationTelemetryEventType) =
    telemetryClient.trackEvent(telemetryEventType.value, telemetryEventType.customDimensions(conversation))
}
