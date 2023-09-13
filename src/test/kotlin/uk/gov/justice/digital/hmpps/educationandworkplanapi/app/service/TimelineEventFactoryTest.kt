package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidStep
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.anotherValidStep
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent.Companion.newTimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class TimelineEventFactoryTest {

  @InjectMocks
  private lateinit var timelineEventFactory: TimelineEventFactory

  @Test
  fun `should create action plan created event`() {
    // Given
    val actionPlan = aValidActionPlan()

    // When
    val actual = timelineEventFactory.actionPlanCreatedEvent(actionPlan)

    // Then
    assertThat(actual.sourceReference).isEqualTo(actionPlan.reference.toString())
    assertThat(actual.eventType).isEqualTo(TimelineEventType.ACTION_PLAN_CREATED)
    assertThat(actual.prisonId).isEqualTo(actionPlan.goals[0].createdAtPrison)
    assertThat(actual.createdBy).isEqualTo(actionPlan.goals[0].lastUpdatedBy)
    assertThat(actual.createdByDisplayName).isEqualTo(actionPlan.goals[0].lastUpdatedByDisplayName)
    assertThat(actual.reference).isNotNull()
    assertThat(actual.timestamp).isNotNull()
  }

  @Test
  fun `should create goal created event`() {
    // Given
    val goal = aValidGoal()

    // When
    val actual = timelineEventFactory.goalCreatedTimelineEvent(goal)

    // Then
    assertThat(actual.sourceReference).isEqualTo(goal.reference.toString())
    assertThat(actual.eventType).isEqualTo(TimelineEventType.GOAL_CREATED)
    assertThat(actual.prisonId).isEqualTo(goal.createdAtPrison)
    assertThat(actual.createdBy).isEqualTo(goal.lastUpdatedBy)
    assertThat(actual.createdByDisplayName).isEqualTo(goal.lastUpdatedByDisplayName)
    assertThat(actual.reference).isNotNull()
    assertThat(actual.timestamp).isNotNull()
  }

  @Test
  fun `should create goal updated event`() {
    // Given
    val reference = UUID.randomUUID()
    val previousGoal = aValidGoal(reference = reference, title = "Learn French")
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
    val actual = timelineEventFactory.goalUpdatedEvents(previousGoal = previousGoal, updatedGoal = updatedGoal)

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
  fun `should create goal completed event`(
    originalStatus: GoalStatus,
    newStatus: GoalStatus,
    expectedEventType: TimelineEventType,
  ) {
    // Given
    val reference = UUID.randomUUID()
    val previousGoal = aValidGoal(reference = reference, status = originalStatus)
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
    val actual = timelineEventFactory.goalUpdatedEvents(previousGoal = previousGoal, updatedGoal = updatedGoal)

    // Then
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFields("reference", "timestamp")
      .isEqualTo(expectedEvents)
  }

  @Test
  fun `should create step started event`() {
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
    val previousGoal = aValidGoal(reference = goalReference, steps = originalSteps)
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
    val actual = timelineEventFactory.goalUpdatedEvents(previousGoal = previousGoal, updatedGoal = updatedGoal)

    // Then
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFields("reference", "timestamp")
      .isEqualTo(expectedEvents)
  }

  @Test
  fun `should create multiple update events`() {
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
    val previousGoal =
      aValidGoal(reference = goalReference, title = "Learn French", status = GoalStatus.ACTIVE, steps = originalSteps)
    val updatedGoal = aValidGoal(
      reference = goalReference,
      title = "Learn Spanish",
      status = GoalStatus.COMPLETED,
      steps = updatedSteps,
    )

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
    val actual = timelineEventFactory.goalUpdatedEvents(previousGoal = previousGoal, updatedGoal = updatedGoal)

    // Then
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFields("reference", "timestamp")
      .isEqualTo(expectedEvents)
  }
}
