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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.assertThat
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class TimelineEventFactoryTest {

  companion object {
    val IGNORED_FIELDS = arrayOf("reference", "timestamp", "correlationId")
  }

  @InjectMocks
  private lateinit var timelineEventFactory: TimelineEventFactory

  @Test
  fun `should create action plan created event`() {
    // Given
    val goal = aValidGoal()
    val actionPlan = aValidActionPlan(goals = listOf(goal))

    // When
    val actual = timelineEventFactory.actionPlanCreatedEvent(actionPlan)

    // Then
    assertThat(actual).hasSize(2)
    val actionPlanCreatedEvent = actual[0]
    assertThat(actionPlanCreatedEvent)
      .hasSourceReference(actionPlan.reference.toString())
      .hasEventType(TimelineEventType.ACTION_PLAN_CREATED)
      .hasPrisonId(goal.createdAtPrison)
      .wasActionedBy(goal.lastUpdatedBy!!)
      .wasActionedByDisplayName(goal.lastUpdatedByDisplayName!!)
      .hasNoContextualInfo()

    val goalCreatedEvent = actual[1]
    assertThat(goalCreatedEvent)
      .hasSourceReference(goal.reference.toString())
      .hasEventType(TimelineEventType.GOAL_CREATED)
      .hasPrisonId(goal.createdAtPrison)
      .wasActionedBy(goal.lastUpdatedBy!!)
      .wasActionedByDisplayName(goal.lastUpdatedByDisplayName!!)
      .hasContextualInfo(goal.title)
      .hasCorrelationId(actionPlanCreatedEvent.correlationId)
  }

  @Test
  fun `should create goal created event`() {
    // Given
    val goal = aValidGoal()

    // When
    val actual = timelineEventFactory.goalCreatedTimelineEvent(goal)

    // Then
    assertThat(actual)
      .hasSourceReference(goal.reference.toString())
      .hasEventType(TimelineEventType.GOAL_CREATED)
      .hasPrisonId(goal.createdAtPrison)
      .wasActionedBy(goal.lastUpdatedBy!!)
      .wasActionedByDisplayName(goal.lastUpdatedByDisplayName!!)
      .hasContextualInfo(goal.title)
  }

  @Test
  fun `should create goal updated event given only goal changed`() {
    // Given
    val reference = UUID.randomUUID()
    val previousGoal = aValidGoal(reference = reference, title = "Learn French")
    val updatedGoal = aValidGoal(reference = reference, title = "Learn Spanish")
    val expectedEvents = listOf(
      newTimelineEvent(
        sourceReference = reference.toString(),
        eventType = TimelineEventType.GOAL_UPDATED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        actionedBy = updatedGoal.lastUpdatedBy!!,
        actionedByDisplayName = updatedGoal.lastUpdatedByDisplayName!!,
        contextualInfo = updatedGoal.title,
      ),
    )

    // When
    val actual = timelineEventFactory.goalUpdatedEvents(previousGoal = previousGoal, updatedGoal = updatedGoal)

    // Then
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFields(*IGNORED_FIELDS)
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
  fun `should create goal status change event given only goal status changed`(
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
        actionedBy = updatedGoal.lastUpdatedBy!!,
        actionedByDisplayName = updatedGoal.lastUpdatedByDisplayName!!,
        contextualInfo = updatedGoal.title,
      ),
    )

    // When
    val actual = timelineEventFactory.goalUpdatedEvents(previousGoal = previousGoal, updatedGoal = updatedGoal)

    // Then
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFields(*IGNORED_FIELDS)
      .isEqualTo(expectedEvents)
  }

  @Test
  fun `should create goal updated event and step updated event given only step changed`() {
    // Given
    val goalReference = UUID.randomUUID()
    val step1Reference = UUID.randomUUID()
    val step2Reference = UUID.randomUUID()
    val previousSteps = listOf(
      aValidStep(reference = step1Reference, title = "Book French course"),
      anotherValidStep(reference = step2Reference),
    )
    val updatedSteps = listOf(
      aValidStep(reference = step1Reference, title = "Book Spanish course"),
      anotherValidStep(reference = step2Reference),
    )
    val previousGoal = aValidGoal(reference = goalReference, steps = previousSteps)
    val updatedGoal = aValidGoal(reference = goalReference, steps = updatedSteps)
    val expectedEvents = listOf(
      newTimelineEvent(
        sourceReference = goalReference.toString(),
        eventType = TimelineEventType.GOAL_UPDATED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        actionedBy = updatedGoal.lastUpdatedBy!!,
        actionedByDisplayName = updatedGoal.lastUpdatedByDisplayName!!,
        contextualInfo = previousGoal.title,
      ),
      newTimelineEvent(
        sourceReference = step1Reference.toString(),
        eventType = TimelineEventType.STEP_UPDATED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        actionedBy = updatedGoal.lastUpdatedBy!!,
        actionedByDisplayName = updatedGoal.lastUpdatedByDisplayName!!,
        contextualInfo = "Book Spanish course",
      ),
    )

    // When
    val actual = timelineEventFactory.goalUpdatedEvents(previousGoal = previousGoal, updatedGoal = updatedGoal)

    // Then
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFields(*IGNORED_FIELDS)
      .isEqualTo(expectedEvents)
  }

  @Test
  fun `should create goal updated event & step status change event given only step status changed`() {
    // Given
    val goalReference = UUID.randomUUID()
    val step1Reference = UUID.randomUUID()
    val step2Reference = UUID.randomUUID()
    val originalSteps = listOf(
      aValidStep(reference = step1Reference, "Book course", status = StepStatus.NOT_STARTED),
      anotherValidStep(reference = step2Reference),
    )
    val updatedSteps = listOf(
      aValidStep(reference = step1Reference, "Book course", status = StepStatus.ACTIVE),
      anotherValidStep(reference = step2Reference),
    )
    val previousGoal = aValidGoal(reference = goalReference, steps = originalSteps)
    val updatedGoal = aValidGoal(reference = goalReference, steps = updatedSteps)
    val expectedEvents = listOf(
      newTimelineEvent(
        sourceReference = goalReference.toString(),
        eventType = TimelineEventType.GOAL_UPDATED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        actionedBy = updatedGoal.lastUpdatedBy!!,
        actionedByDisplayName = updatedGoal.lastUpdatedByDisplayName!!,
        contextualInfo = previousGoal.title,
      ),
      newTimelineEvent(
        sourceReference = step1Reference.toString(),
        eventType = TimelineEventType.STEP_STARTED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        actionedBy = updatedGoal.lastUpdatedBy!!,
        actionedByDisplayName = updatedGoal.lastUpdatedByDisplayName!!,
        contextualInfo = "Book course",
      ),
    )

    // When
    val actual = timelineEventFactory.goalUpdatedEvents(previousGoal = previousGoal, updatedGoal = updatedGoal)

    // Then
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFields(*IGNORED_FIELDS)
      .isEqualTo(expectedEvents)
  }

  @Test
  fun `should create multiple STATUS change events`() {
    // Given
    val goalReference = UUID.randomUUID()
    val step1Reference = UUID.randomUUID()
    val step2Reference = UUID.randomUUID()
    val originalSteps = listOf(
      aValidStep(reference = step1Reference, title = "Book French course", status = StepStatus.ACTIVE),
      anotherValidStep(reference = step2Reference, title = "Complete course", status = StepStatus.NOT_STARTED),
    )
    val updatedSteps = listOf(
      // title and status of first step changed
      aValidStep(reference = step1Reference, title = "Book Spanish course", status = StepStatus.COMPLETE),
      // status of second step changed
      anotherValidStep(reference = step2Reference, title = "Complete course", status = StepStatus.ACTIVE),
    )
    val previousGoal =
      aValidGoal(
        reference = goalReference,
        title = "Learn French",
        status = GoalStatus.ACTIVE,
        steps = originalSteps,
      )
    // title and status of goal changed
    val updatedGoal = aValidGoal(
      reference = goalReference,
      title = "Learn Spanish",
      status = GoalStatus.COMPLETED,
      steps = updatedSteps,
    )

    // changes to the status of a Goal/Step take precedence over changes to the Goal/Step themselves, so GOAL_UPDATED & STEP_UPDATED events are not returned
    val expectedEvents = listOf(
      newTimelineEvent(
        sourceReference = goalReference.toString(),
        eventType = TimelineEventType.GOAL_COMPLETED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        actionedBy = updatedGoal.lastUpdatedBy!!,
        actionedByDisplayName = updatedGoal.lastUpdatedByDisplayName!!,
        contextualInfo = "Learn Spanish",
      ),
      newTimelineEvent(
        sourceReference = goalReference.toString(),
        eventType = TimelineEventType.GOAL_UPDATED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        actionedBy = updatedGoal.lastUpdatedBy!!,
        actionedByDisplayName = updatedGoal.lastUpdatedByDisplayName!!,
        contextualInfo = "Learn Spanish",
      ),
      newTimelineEvent(
        sourceReference = step1Reference.toString(),
        eventType = TimelineEventType.STEP_COMPLETED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        actionedBy = updatedGoal.lastUpdatedBy!!,
        actionedByDisplayName = updatedGoal.lastUpdatedByDisplayName!!,
        contextualInfo = "Book Spanish course",
      ),
      newTimelineEvent(
        sourceReference = step1Reference.toString(),
        eventType = TimelineEventType.STEP_UPDATED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        actionedBy = updatedGoal.lastUpdatedBy!!,
        actionedByDisplayName = updatedGoal.lastUpdatedByDisplayName!!,
        contextualInfo = "Book Spanish course",
      ),
      newTimelineEvent(
        sourceReference = step2Reference.toString(),
        eventType = TimelineEventType.STEP_STARTED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        actionedBy = updatedGoal.lastUpdatedBy!!,
        actionedByDisplayName = updatedGoal.lastUpdatedByDisplayName!!,
        contextualInfo = "Complete course",
      ),
    )

    // When
    val actual = timelineEventFactory.goalUpdatedEvents(previousGoal = previousGoal, updatedGoal = updatedGoal)

    // Then
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(".*reference", ".*timestamp", ".*correlationId")
      .isEqualTo(expectedEvents)
    val expectedCorrelationId = actual[0].correlationId
    assertThat(actual).allMatch { it.correlationId == expectedCorrelationId }
  }
}
