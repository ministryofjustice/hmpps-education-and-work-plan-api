package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Induction
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.UpdatedInductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.CompletedReview
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.UpdatedReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.trackEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.GOAL_ARCHIVED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.GOAL_COMPLETED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.GOAL_CREATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.GOAL_UNARCHIVED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.GOAL_UPDATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.STEP_REMOVED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.InductionScheduleTelemetryEventType.INDUCTION_SCHEDULE_CREATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.InductionScheduleTelemetryEventType.INDUCTION_SCHEDULE_UPDATED
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

  fun trackInductionScheduleCreated(inductionSchedule: InductionSchedule) {
    sendTelemetryEventForInductionSchedule(
      inductionSchedule = inductionSchedule,
      telemetryEventType = INDUCTION_SCHEDULE_CREATED,
    )
  }

  fun trackInductionScheduleUpdated(inductionSchedule: InductionSchedule) {
    sendTelemetryEventForInductionSchedule(
      inductionSchedule = inductionSchedule,
      telemetryEventType = INDUCTION_SCHEDULE_UPDATED,
    )
  }

  fun trackInductionUpdated(induction: Induction) {
    sendTelemetryEventForInduction(
      induction = induction,
      telemetryEventType = INDUCTION_UPDATED,
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

  fun trackReviewCompleted(completedReview: CompletedReview) {
    telemetryClient.trackEvent(
      CompletedReviewTelemetryEventType.REVIEW_COMPLETED.value,
      CompletedReviewTelemetryEventType.REVIEW_COMPLETED.customDimensions(completedReview),
    )
  }

  fun trackReviewScheduleStatusUpdated(updatedReviewScheduleStatus: UpdatedReviewScheduleStatus) {
    telemetryClient.trackEvent(
      UpdatedReviewScheduleStatusTelemetryEventType.REVIEW_SCHEDULE_STATUS_UPDATED.value,
      UpdatedReviewScheduleStatusTelemetryEventType.REVIEW_SCHEDULE_STATUS_UPDATED.customDimensions(updatedReviewScheduleStatus),
    )
  }

  fun trackReviewScheduleCreated(reviewSchedule: ReviewSchedule) {
    telemetryClient.trackEvent(
      ReviewScheduleCreatedTelemetryEventType.REVIEW_SCHEDULE_CREATED.value,
      ReviewScheduleCreatedTelemetryEventType.REVIEW_SCHEDULE_CREATED.customDimensions(reviewSchedule),
    )
  }

  fun trackInductionScheduleStatusUpdated(updatedInductionScheduleStatus: UpdatedInductionScheduleStatus) {
    telemetryClient.trackEvent(
      UpdatedInductionScheduleStatusTelemetryEventType.INDUCTION_SCHEDULE_STATUS_UPDATED.value,
      UpdatedInductionScheduleStatusTelemetryEventType.INDUCTION_SCHEDULE_STATUS_UPDATED.customDimensions(updatedInductionScheduleStatus),
    )
  }

  private fun sendTelemetryEventForGoal(goal: Goal, correlationId: UUID, telemetryEventType: GoalTelemetryEventType) =
    telemetryClient.trackEvent(telemetryEventType.value, telemetryEventType.customDimensions(goal, correlationId))

  private fun sendTelemetryEventForInduction(induction: Induction, telemetryEventType: InductionTelemetryEventType) =
    telemetryClient.trackEvent(telemetryEventType.value, telemetryEventType.customDimensions(induction))

  private fun sendTelemetryEventForInductionSchedule(inductionSchedule: InductionSchedule, telemetryEventType: InductionScheduleTelemetryEventType) =
    telemetryClient.trackEvent(telemetryEventType.value, telemetryEventType.customDimensions(inductionSchedule))
}
