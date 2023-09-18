package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import com.microsoft.applicationinsights.TelemetryClient
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.trackEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidStep
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class TelemetryServiceTest {

  @Mock
  private lateinit var telemetryClient: TelemetryClient

  @InjectMocks
  private lateinit var telemetryService: TelemetryService

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
    telemetryService.trackGoalCreateEvent(goal)

    // Then
    verify(telemetryClient).trackEvent("goal-create", expectedEventProperties)
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
    telemetryService.trackGoalCreateEvent(goal)

    // Then
    verify(telemetryClient).trackEvent("goal-create", expectedEventProperties)
  }

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
    telemetryService.trackGoalUpdateEvent(goal)

    // Then
    verify(telemetryClient).trackEvent("goal-update", expectedEventProperties)
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
    telemetryService.trackGoalUpdateEvent(goal)

    // Then
    verify(telemetryClient).trackEvent("goal-update", expectedEventProperties)
  }
}
