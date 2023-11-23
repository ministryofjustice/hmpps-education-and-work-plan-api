package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.STEP_ADDED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.GoalTelemetryEventType.STEP_REMOVED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidStep
import java.time.LocalDate
import java.util.UUID

class TelemetryEventTypeResolverTest {

  private val resolver = TelemetryEventTypeResolver()

  @Test
  fun `should get goal update types given goals with no differences`() {
    // Given
    val goalReference = aValidReference()
    val stepReference = aValidReference()

    val previousSteps = listOf(aValidStep(reference = stepReference))
    val previousGoal = aValidGoal(
      reference = goalReference,
      title = "Learn French",
      steps = previousSteps,
    )
    val updatedSteps = listOf(aValidStep(reference = stepReference))
    val updatedGoal = aValidGoal(
      reference = goalReference,
      title = "Learn French",
      steps = updatedSteps,
    )

    val expectedTelemetryEventTypes = emptyList<GoalTelemetryEventType>()

    // When
    val actual = resolver.resolveUpdateEventTypes(previousGoal, updatedGoal)

    // Then
    assertThat(actual).isEqualTo(expectedTelemetryEventTypes)
  }

  @Test
  fun `should get goal update types given goals with different titles`() {
    // Given
    val goalReference = aValidReference()
    val stepReference = aValidReference()

    val previousSteps = listOf(aValidStep(reference = stepReference))
    val previousGoal = aValidGoal(
      reference = goalReference,
      title = "Learn French",
      steps = previousSteps,
    )
    val updatedSteps = listOf(aValidStep(reference = stepReference))
    val updatedGoal = aValidGoal(
      reference = goalReference,
      title = "Learn Spanish",
      steps = updatedSteps,
    )
    val expectedTelemetryEventTypes = listOf(GoalTelemetryEventType.GOAL_UPDATED)

    // When
    val actual = resolver.resolveUpdateEventTypes(previousGoal, updatedGoal)

    // Then
    assertThat(actual).isEqualTo(expectedTelemetryEventTypes)
  }

  @Test
  fun `should get goal update types given goals with different notes`() {
    // Given
    val goalReference = aValidReference()
    val stepReference = aValidReference()

    val previousSteps = listOf(aValidStep(reference = stepReference))
    val previousGoal = aValidGoal(
      reference = goalReference,
      title = "Learn French",
      steps = previousSteps,
      notes = "The course is available from May",
    )
    val updatedSteps = listOf(aValidStep(reference = stepReference))
    val updatedGoal = aValidGoal(
      reference = goalReference,
      title = "Learn French",
      steps = updatedSteps,
      notes = "A suitable course is available from May",
    )
    val expectedTelemetryEventTypes = listOf(GoalTelemetryEventType.GOAL_UPDATED)

    // When
    val actual = resolver.resolveUpdateEventTypes(previousGoal, updatedGoal)

    // Then
    assertThat(actual).isEqualTo(expectedTelemetryEventTypes)
  }

  @Test
  fun `should get goal update types given goals with different lastUpdatedAtPrisons`() {
    // Given
    val goalReference = aValidReference()
    val stepReference = aValidReference()

    val previousSteps = listOf(aValidStep(reference = stepReference))
    val previousGoal = aValidGoal(
      reference = goalReference,
      title = "Learn French",
      steps = previousSteps,
      lastUpdatedAtPrison = "MDI",
    )
    val updatedSteps = listOf(aValidStep(reference = stepReference))
    val updatedGoal = aValidGoal(
      reference = goalReference,
      title = "Learn French",
      steps = updatedSteps,
      lastUpdatedAtPrison = "BXI",
    )
    val expectedTelemetryEventTypes = listOf(GoalTelemetryEventType.GOAL_UPDATED)

    // When
    val actual = resolver.resolveUpdateEventTypes(previousGoal, updatedGoal)

    // Then
    assertThat(actual).isEqualTo(expectedTelemetryEventTypes)
  }

  @Test
  fun `should get goal update types given goals with different targetCompletionDate`() {
    // Given
    val goalReference = aValidReference()
    val stepReference = aValidReference()

    val previousSteps = listOf(aValidStep(reference = stepReference))
    val previousGoal = aValidGoal(
      reference = goalReference,
      title = "Learn French",
      steps = previousSteps,
      targetCompletionDate = LocalDate.parse("2024-01-31"),
    )
    val updatedSteps = listOf(aValidStep(reference = stepReference))
    val updatedGoal = aValidGoal(
      reference = goalReference,
      title = "Learn French",
      steps = updatedSteps,
      targetCompletionDate = LocalDate.parse("2024-06-30"),
    )
    val expectedTelemetryEventTypes = listOf(GoalTelemetryEventType.GOAL_UPDATED)

    // When
    val actual = resolver.resolveUpdateEventTypes(previousGoal, updatedGoal)

    // Then
    assertThat(actual).isEqualTo(expectedTelemetryEventTypes)
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "ACTIVE, COMPLETED, GOAL_COMPLETED",
      "ACTIVE, ARCHIVED, GOAL_ARCHIVED",
      "ARCHIVED, ACTIVE, GOAL_STARTED",
    ],
  )
  fun `should get goal update types given goals with different statuses`(
    previousGoalStatus: GoalStatus,
    updatedGoalStatus: GoalStatus,
    expectedTelemetryEventType: GoalTelemetryEventType,
  ) {
    // Given
    val goalReference = aValidReference()
    val stepReference = aValidReference()

    val previousSteps = listOf(aValidStep(reference = stepReference))
    val previousGoal = aValidGoal(
      reference = goalReference,
      title = "Learn French",
      status = previousGoalStatus,
      steps = previousSteps,
    )
    val updatedSteps = listOf(aValidStep(reference = stepReference))
    val updatedGoal = aValidGoal(
      reference = goalReference,
      title = "Learn French",
      status = updatedGoalStatus,
      steps = updatedSteps,
    )
    val expectedTelemetryUpdateEventTypes = listOf(expectedTelemetryEventType)

    // When
    val actual = resolver.resolveUpdateEventTypes(previousGoal, updatedGoal)

    // Then
    assertThat(actual).isEqualTo(expectedTelemetryUpdateEventTypes)
  }

  @Test
  fun `should get goal update types given steps deleted from update goal`() {
    // Given
    val goalReference = aValidReference()
    val stepReference = aValidReference()

    val previousSteps = listOf(
      aValidStep(reference = stepReference),
      aValidStep(),
      aValidStep(),
    )
    val previousGoal = aValidGoal(
      reference = goalReference,
      title = "Learn French",
      steps = previousSteps,
    )
    val updatedSteps = listOf(aValidStep(reference = stepReference))
    val updatedGoal = aValidGoal(
      reference = goalReference,
      title = "Learn French",
      steps = updatedSteps,
    )
    val expectedTelemetryEventTypes = listOf(STEP_REMOVED, STEP_REMOVED)

    // When
    val actual = resolver.resolveUpdateEventTypes(previousGoal, updatedGoal)

    // Then
    assertThat(actual).isEqualTo(expectedTelemetryEventTypes)
  }

  @Test
  fun `should get goal update types given steps added in update goal`() {
    // Given
    val goalReference = aValidReference()
    val stepReference = aValidReference()

    val previousSteps = listOf(aValidStep(reference = stepReference))
    val previousGoal = aValidGoal(
      reference = goalReference,
      title = "Learn French",
      steps = previousSteps,
    )
    val updatedSteps = listOf(
      aValidStep(reference = stepReference),
      aValidStep(),
    )
    val updatedGoal = aValidGoal(
      reference = goalReference,
      title = "Learn French",
      steps = updatedSteps,
    )
    val expectedTelemetryEventTypes = listOf(STEP_ADDED)

    // When
    val actual = resolver.resolveUpdateEventTypes(previousGoal, updatedGoal)

    // Then
    assertThat(actual).isEqualTo(expectedTelemetryEventTypes)
  }

  @Test
  fun `should get goal update types given one step added and one step removed in update goal`() {
    // Given
    val goalReference = aValidReference()
    val stepReference = aValidReference()

    val previousSteps = listOf(
      aValidStep(reference = stepReference),
      aValidStep(reference = UUID.randomUUID(), title = "Some step"),
    )
    val previousGoal = aValidGoal(
      reference = goalReference,
      title = "Learn French",
      steps = previousSteps,
    )
    val updatedSteps = listOf(
      aValidStep(reference = stepReference),
      aValidStep(reference = UUID.randomUUID(), title = "Some entirely different step"),
    )
    val updatedGoal = aValidGoal(
      reference = goalReference,
      title = "Learn French",
      steps = updatedSteps,
    )
    val expectedTelemetryEventTypes = listOf(STEP_REMOVED, STEP_ADDED)

    // When
    val actual = resolver.resolveUpdateEventTypes(previousGoal, updatedGoal)

    // Then
    assertThat(actual).isEqualTo(expectedTelemetryEventTypes)
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "ACTIVE, COMPLETE, STEP_COMPLETED",
      "ACTIVE, NOT_STARTED, STEP_NOT_STARTED",
      "NOT_STARTED, ACTIVE, STEP_STARTED",
    ],
  )
  fun `should get goal update types given a step with different statuses`(
    previousStepStatus: StepStatus,
    updatedStepStatus: StepStatus,
    expectedTelemetryEventType: GoalTelemetryEventType,
  ) {
    // Given
    val goalReference = aValidReference()
    val stepReference = aValidReference()

    val previousSteps = listOf(
      aValidStep(reference = stepReference, status = previousStepStatus),
    )
    val previousGoal = aValidGoal(
      reference = goalReference,
      title = "Learn French",
      steps = previousSteps,
    )
    val updatedSteps = listOf(
      aValidStep(reference = stepReference, status = updatedStepStatus),
    )
    val updatedGoal = aValidGoal(
      reference = goalReference,
      title = "Learn French",
      steps = updatedSteps,
    )
    val expectedTelemetryUpdateEventTypes = listOf(expectedTelemetryEventType)

    // When
    val actual = resolver.resolveUpdateEventTypes(previousGoal, updatedGoal)

    // Then
    assertThat(actual).isEqualTo(expectedTelemetryUpdateEventTypes)
  }
}
