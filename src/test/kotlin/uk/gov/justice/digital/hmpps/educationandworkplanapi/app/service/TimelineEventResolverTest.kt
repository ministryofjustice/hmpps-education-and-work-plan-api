package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.event.TimelineEventResolver
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidStep
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.anotherValidStep
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent.Companion.newTimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class TimelineEventResolverTest {
  @InjectMocks
  private lateinit var timelineEventResolver: TimelineEventResolver

  @Test
  fun `should resolve goal updated event`() {
    // Given
    val reference = UUID.randomUUID()
    val existingGoal = aValidGoal(reference = reference, title = "Learn French")
    val updatedGoal = aValidGoal(reference = reference, title = "Learn Spanish")
    val expectedEvents = listOf(
      newTimelineEvent(
        sourceReference = reference.toString(),
        eventType = TimelineEventType.GOAL_UPDATED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        createdBy = updatedGoal.lastUpdatedBy!!,
        createdByDisplayName = updatedGoal.lastUpdatedByDisplayName!!,
      ),
    )

    // When
    val actual = timelineEventResolver.resolveGoalUpdatedEvents(updatedGoal = updatedGoal, existingGoal = existingGoal)

    // Then
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFields("reference", "timestamp")
      .isEqualTo(expectedEvents)
    assertThat(actual[0].reference).isNotNull
    assertThat(actual[0].timestamp).isNotNull
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "ACTIVE, COMPLETED, GOAL_COMPLETED",
      "ACTIVE, ARCHIVED, GOAL_ARCHIVED",
      "ARCHIVED, ACTIVE, GOAL_STARTED",
    ],
  )
  fun `should resolve goal completed event`(
    originalStatus: GoalStatus,
    newStatus: GoalStatus,
    expectedEventType: TimelineEventType,
  ) {
    // Given
    val reference = UUID.randomUUID()
    val existingGoal = aValidGoal(reference = reference, status = originalStatus)
    val updatedGoal = aValidGoal(reference = reference, status = newStatus)
    val expectedEvents = listOf(
      newTimelineEvent(
        sourceReference = reference.toString(),
        eventType = expectedEventType,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        createdBy = updatedGoal.lastUpdatedBy!!,
        createdByDisplayName = updatedGoal.lastUpdatedByDisplayName!!,
      ),
    )

    // When
    val actual = timelineEventResolver.resolveGoalUpdatedEvents(updatedGoal = updatedGoal, existingGoal = existingGoal)

    // Then
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFields("reference", "timestamp")
      .isEqualTo(expectedEvents)
  }

  @Test
  fun `should resolve step started event`() {
    // Given
    val goalReference = UUID.randomUUID()
    val step1Reference = UUID.randomUUID()
    val step2Reference = UUID.randomUUID()
    val originalSteps = listOf(
      aValidStep(reference = step1Reference, status = StepStatus.NOT_STARTED),
      anotherValidStep(reference = step2Reference),
    )
    val updatedSteps = listOf(
      aValidStep(reference = step1Reference, status = StepStatus.ACTIVE),
      anotherValidStep(reference = step2Reference),
    )
    val existingGoal = aValidGoal(reference = goalReference, steps = originalSteps)
    val updatedGoal = aValidGoal(reference = goalReference, steps = updatedSteps)
    val expectedEvents = listOf(
      newTimelineEvent(
        sourceReference = step1Reference.toString(),
        eventType = TimelineEventType.STEP_STARTED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        createdBy = updatedGoal.lastUpdatedBy!!,
        createdByDisplayName = updatedGoal.lastUpdatedByDisplayName!!,
      ),
    )

    // When
    val actual = timelineEventResolver.resolveGoalUpdatedEvents(updatedGoal = updatedGoal, existingGoal = existingGoal)

    // Then
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFields("reference", "timestamp")
      .isEqualTo(expectedEvents)
  }

  @Test
  fun `should resolve multiple update events`() {
    // Given
    val goalReference = UUID.randomUUID()
    val step1Reference = UUID.randomUUID()
    val step2Reference = UUID.randomUUID()
    val originalSteps = listOf(
      aValidStep(reference = step1Reference, status = StepStatus.ACTIVE),
      anotherValidStep(reference = step2Reference, status = StepStatus.ACTIVE),
    )
    val updatedSteps = listOf(
      aValidStep(reference = step1Reference, status = StepStatus.COMPLETE),
      anotherValidStep(reference = step2Reference, status = StepStatus.COMPLETE),
    )
    val existingGoal = aValidGoal(reference = goalReference, title = "Learn French", status = GoalStatus.ACTIVE, steps = originalSteps)
    val updatedGoal = aValidGoal(reference = goalReference, title = "Learn Spanish", status = GoalStatus.COMPLETED, steps = updatedSteps)

    val expectedEvents = listOf(
      newTimelineEvent(
        sourceReference = goalReference.toString(),
        eventType = TimelineEventType.GOAL_UPDATED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        createdBy = updatedGoal.lastUpdatedBy!!,
        createdByDisplayName = updatedGoal.lastUpdatedByDisplayName!!,
      ),
      newTimelineEvent(
        sourceReference = goalReference.toString(),
        eventType = TimelineEventType.GOAL_COMPLETED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        createdBy = updatedGoal.lastUpdatedBy!!,
        createdByDisplayName = updatedGoal.lastUpdatedByDisplayName!!,
      ),
      newTimelineEvent(
        sourceReference = step1Reference.toString(),
        eventType = TimelineEventType.STEP_COMPLETED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        createdBy = updatedGoal.lastUpdatedBy!!,
        createdByDisplayName = updatedGoal.lastUpdatedByDisplayName!!,
      ),
      newTimelineEvent(
        sourceReference = step2Reference.toString(),
        eventType = TimelineEventType.STEP_COMPLETED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        createdBy = updatedGoal.lastUpdatedBy!!,
        createdByDisplayName = updatedGoal.lastUpdatedByDisplayName!!,
      ),
    )

    // When
    val actual = timelineEventResolver.resolveGoalUpdatedEvents(updatedGoal = updatedGoal, existingGoal = existingGoal)

    // Then
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFields("reference", "timestamp")
      .isEqualTo(expectedEvents)
  }
}
