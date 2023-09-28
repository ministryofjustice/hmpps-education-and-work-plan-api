package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import com.microsoft.applicationinsights.TelemetryClient
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.trackEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidStep
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class TelemetryServiceTest {

  @Mock
  private lateinit var telemetryClient: TelemetryClient

  @Mock
  private lateinit var telemetryUpdateEventTypeResolver: TelemetryUpdateEventTypeResolver

  @InjectMocks
  private lateinit var telemetryService: TelemetryService

  @Nested
  inner class TrackGoalCreatedEvent {
    @Test
    fun `should track create goal event given goal with no notes`() {
      // Given
      val reference = UUID.randomUUID()
      val status = GoalStatus.ACTIVE
      val steps = listOf(aValidStep(), aValidStep(), aValidStep())

      val goal = aValidGoal(
        reference = reference,
        status = status,
        steps = steps,
        notes = null,
      )

      val expectedEventProperties = mapOf(
        "reference" to reference.toString(),
        "status" to "ACTIVE",
        "stepCount" to "3",
        "notesCharacterCount" to "0",
      )

      // When
      telemetryService.trackGoalCreatedEvent(goal)

      // Then
      verify(telemetryClient).trackEvent("goal-created", expectedEventProperties)
    }

    @Test
    fun `should track create goal event given goal with some notes`() {
      // Given
      val reference = UUID.randomUUID()
      val status = GoalStatus.ACTIVE
      val steps = listOf(aValidStep(), aValidStep(), aValidStep())

      val goal = aValidGoal(
        reference = reference,
        status = status,
        steps = steps,
        notes = "Some notes about the goal",
      )

      val expectedEventProperties = mapOf(
        "reference" to reference.toString(),
        "status" to "ACTIVE",
        "stepCount" to "3",
        "notesCharacterCount" to "25",
      )

      // When
      telemetryService.trackGoalCreatedEvent(goal)

      // Then
      verify(telemetryClient).trackEvent("goal-created", expectedEventProperties)
    }
  }

  @Nested
  inner class TrackGoalUpdatedEvent {
    @Test
    fun `should track update goal event given goal with no notes`() {
      // Given
      val reference = UUID.randomUUID()
      val status = GoalStatus.ACTIVE
      val steps = listOf(aValidStep(), aValidStep(), aValidStep())

      val goal = aValidGoal(
        reference = reference,
        status = status,
        steps = steps,
        notes = null,
      )

      val expectedEventProperties = mapOf(
        "reference" to reference.toString(),
        "notesCharacterCount" to "0",
      )

      // When
      telemetryService.trackGoalUpdatedEvent(goal)

      // Then
      verify(telemetryClient).trackEvent("goal-updated", expectedEventProperties)
    }

    @Test
    fun `should track update goal event given goal with notes`() {
      // Given
      val reference = UUID.randomUUID()
      val status = GoalStatus.ACTIVE
      val steps = listOf(aValidStep(), aValidStep(), aValidStep())

      val goal = aValidGoal(
        reference = reference,
        status = status,
        steps = steps,
        notes = "Chris wants to become a chef on release so basic food hygiene course will be useful.",
      )

      val expectedEventProperties = mapOf(
        "reference" to reference.toString(),
        "notesCharacterCount" to "84",
      )

      // When
      telemetryService.trackGoalUpdatedEvent(goal)

      // Then
      verify(telemetryClient).trackEvent("goal-updated", expectedEventProperties)
    }
  }

  @Nested
  inner class TrackStepRemovedEvent {
    @Test
    fun `should track remove step event`() {
      // Given
      val reference = UUID.randomUUID()
      val steps = listOf(aValidStep(), aValidStep(), aValidStep())

      val goal = aValidGoal(
        reference = reference,
        steps = steps,
      )

      val expectedEventProperties = mapOf(
        "reference" to reference.toString(),
        "stepCount" to "3",
      )

      // When
      telemetryService.trackStepRemovedEvent(goal)

      // Then
      verify(telemetryClient).trackEvent("step-removed", expectedEventProperties)
    }
  }

  @Nested
  inner class TrackGoalUpdatedEvents {
    @Test
    fun `should track goal updated events given previous and updated goals contain all possible differences`() {
      // Given
      val reference = UUID.randomUUID()
      val previousGoal = aValidGoal(reference = reference)
      val updatedGoal = aValidGoal(reference = reference)

      val updateEventTypes = listOf(
        TelemetryUpdateEventType.GOAL_UPDATED,
        TelemetryUpdateEventType.STEP_REMOVED, // 2 STEP_REMOVED event types to trigger 2 corresponding telemetry events.
        TelemetryUpdateEventType.STEP_REMOVED,
        // The follow event types will be supported in the future.
        // They are included here to break the test as and when the implementation handles them.
        TelemetryUpdateEventType.GOAL_CREATED,
        TelemetryUpdateEventType.GOAL_STARTED,
        TelemetryUpdateEventType.GOAL_COMPLETED,
        TelemetryUpdateEventType.GOAL_ARCHIVED,
        TelemetryUpdateEventType.STEP_UPDATED,
        TelemetryUpdateEventType.STEP_NOT_STARTED,
        TelemetryUpdateEventType.STEP_STARTED,
        TelemetryUpdateEventType.STEP_COMPLETED,
        TelemetryUpdateEventType.STEP_ADDED,
      )
      given(telemetryUpdateEventTypeResolver.resolveUpdateEventTypes(any(), any())).willReturn(updateEventTypes)

      // When
      telemetryService.trackGoalUpdatedEvents(previousGoal, updatedGoal)

      // Then
      verify(telemetryUpdateEventTypeResolver).resolveUpdateEventTypes(previousGoal, updatedGoal)

      verify(telemetryClient).trackEvent(eq("goal-updated"), any(), eq(null))
      verify(telemetryClient, times(2)).trackEvent(eq("step-removed"), any(), eq(null))
      verifyNoMoreInteractions(telemetryClient)
    }
  }
}
