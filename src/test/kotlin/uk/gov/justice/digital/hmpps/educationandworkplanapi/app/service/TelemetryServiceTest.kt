package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.given
import org.mockito.kotlin.secondValue
import org.mockito.kotlin.thirdValue
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import uk.gov.justice.digital.hmpps.domain.goal.GoalStatus
import uk.gov.justice.digital.hmpps.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.domain.goal.aValidStep
import uk.gov.justice.digital.hmpps.domain.induction.aFullyPopulatedInduction
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.trackEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.GOAL_ARCHIVED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.GOAL_COMPLETED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.GOAL_CREATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.GOAL_STARTED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.GOAL_UPDATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.STEP_ADDED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.STEP_COMPLETED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.STEP_NOT_STARTED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.STEP_REMOVED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.STEP_STARTED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.STEP_UPDATED
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class TelemetryServiceTest {

  @Mock
  private lateinit var telemetryClient: TelemetryClient

  @Mock
  private lateinit var telemetryEventTypeResolver: TelemetryEventTypeResolver

  @Captor
  private lateinit var eventPropertiesCaptor: ArgumentCaptor<Map<String, String>>

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

      val correlationId = UUID.randomUUID()
      val expectedEventProperties = mapOf(
        "correlationId" to correlationId.toString(),
        "reference" to reference.toString(),
        "status" to "ACTIVE",
        "stepCount" to "3",
        "notesCharacterCount" to "0",
      )

      // When
      telemetryService.trackGoalCreatedEvent(goal, correlationId)

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

      val correlationId = UUID.randomUUID()
      val expectedEventProperties = mapOf(
        "correlationId" to correlationId.toString(),
        "reference" to reference.toString(),
        "status" to "ACTIVE",
        "stepCount" to "3",
        "notesCharacterCount" to "25",
      )

      // When
      telemetryService.trackGoalCreatedEvent(goal, correlationId)

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

      val correlationId = UUID.randomUUID()
      val expectedEventProperties = mapOf(
        "correlationId" to correlationId.toString(),
        "reference" to reference.toString(),
        "notesCharacterCount" to "0",
      )

      // When
      telemetryService.trackGoalUpdatedEvent(goal, correlationId)

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

      val correlationId = UUID.randomUUID()
      val expectedEventProperties = mapOf(
        "correlationId" to correlationId.toString(),
        "reference" to reference.toString(),
        "notesCharacterCount" to "84",
      )

      // When
      telemetryService.trackGoalUpdatedEvent(goal, correlationId)

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

      val correlationId = UUID.randomUUID()
      val expectedEventProperties = mapOf(
        "correlationId" to correlationId.toString(),
        "reference" to reference.toString(),
        "stepCount" to "3",
      )

      // When
      telemetryService.trackStepRemovedEvent(goal, correlationId)

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
        GOAL_UPDATED,
        // 2 STEP_REMOVED event types to trigger 2 corresponding telemetry events.
        STEP_REMOVED,
        STEP_REMOVED,
        // The follow event types will be supported in the future.
        // They are included here to break the test as and when the implementation handles them.
        GOAL_CREATED,
        GOAL_STARTED,
        GOAL_COMPLETED,
        GOAL_ARCHIVED,
        STEP_UPDATED,
        STEP_NOT_STARTED,
        STEP_STARTED,
        STEP_COMPLETED,
        STEP_ADDED,
      )
      given(telemetryEventTypeResolver.resolveUpdateEventTypes(any(), any())).willReturn(updateEventTypes)

      // When
      telemetryService.trackGoalUpdatedEvents(previousGoal, updatedGoal)

      // Then
      verify(telemetryEventTypeResolver).resolveUpdateEventTypes(previousGoal, updatedGoal)

      verify(telemetryClient).trackEvent(eq("goal-updated"), capture(eventPropertiesCaptor), eq(null))
      verify(telemetryClient, times(2)).trackEvent(eq("step-removed"), capture(eventPropertiesCaptor), eq(null))
      verifyNoMoreInteractions(telemetryClient)

      val propertiesForGoalUpdatedEvent = eventPropertiesCaptor.firstValue
      val propertiesForFirstStepRemovalEvent = eventPropertiesCaptor.secondValue
      val propertiesForSecondStepRemovalEvent = eventPropertiesCaptor.thirdValue
      assertThat(propertiesForGoalUpdatedEvent["correlationId"])
        .isEqualTo(propertiesForFirstStepRemovalEvent["correlationId"])
        .isEqualTo(propertiesForSecondStepRemovalEvent["correlationId"])
    }
  }

  @Nested
  inner class TrackInductionEvents {
    @Test
    fun `should track induction created event`() {
      // Given
      val induction = aFullyPopulatedInduction()
      val expectedEventProperties = mapOf(
        "reference" to induction.reference.toString(),
        "prisonId" to induction.createdAtPrison,
        "userId" to induction.createdBy!!,
      )

      // When
      telemetryService.trackInductionCreated(induction)

      // Then
      verify(telemetryClient).trackEvent("INDUCTION_CREATED", expectedEventProperties)
    }

    @Test
    fun `should track induction updated event`() {
      // Given
      val induction = aFullyPopulatedInduction()
      val expectedEventProperties = mapOf(
        "reference" to induction.reference.toString(),
        "prisonId" to induction.lastUpdatedAtPrison,
        "userId" to induction.lastUpdatedBy!!,
      )

      // When
      telemetryService.trackInductionUpdated(induction)

      // Then
      verify(telemetryClient).trackEvent("INDUCTION_UPDATED", expectedEventProperties)
    }
  }
}
