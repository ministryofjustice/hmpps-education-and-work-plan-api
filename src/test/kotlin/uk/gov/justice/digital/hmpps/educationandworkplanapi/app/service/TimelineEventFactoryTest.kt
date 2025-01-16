package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus
import uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidActionPlan
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidStep
import uk.gov.justice.digital.hmpps.domain.personallearningplan.anotherValidStep
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ReasonToArchiveGoal
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent.Companion.newTimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.domain.timeline.assertThat
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
    val actual = timelineEventFactory.actionPlanCreatedEvent(actionPlan, induction)

    // Then
    assertThat(actual).hasSize(2)
    val actionPlanCreatedEvent = actual[0]
    assertThat(actionPlanCreatedEvent)
      .hasSourceReference(actionPlan.reference.toString())
      .hasEventType(TimelineEventType.ACTION_PLAN_CREATED)
      .hasPrisonId(goal.createdAtPrison)
      .wasActionedBy(goal.lastUpdatedBy!!)

    val goalCreatedEvent = actual[1]
    assertThat(goalCreatedEvent)
      .hasSourceReference(goal.reference.toString())
      .hasEventType(TimelineEventType.GOAL_CREATED)
      .hasPrisonId(goal.createdAtPrison)
      .wasActionedBy(goal.lastUpdatedBy!!)
      .hasContextualInfo(mapOf(TimelineEventContext.GOAL_TITLE to goal.title))
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
      .hasContextualInfo(mapOf(TimelineEventContext.GOAL_TITLE to goal.title))
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
        contextualInfo = mapOf(TimelineEventContext.GOAL_TITLE to updatedGoal.title),
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
        contextualInfo = mapOf(TimelineEventContext.GOAL_TITLE to previousGoal.title),
      ),
      newTimelineEvent(
        sourceReference = step1Reference.toString(),
        eventType = TimelineEventType.STEP_UPDATED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        actionedBy = updatedGoal.lastUpdatedBy!!,
        contextualInfo = mapOf(TimelineEventContext.STEP_TITLE to "Book Spanish course"),
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
        contextualInfo = mapOf(TimelineEventContext.GOAL_TITLE to previousGoal.title),
      ),
      newTimelineEvent(
        sourceReference = step1Reference.toString(),
        eventType = TimelineEventType.STEP_STARTED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        actionedBy = updatedGoal.lastUpdatedBy!!,
        contextualInfo = mapOf(TimelineEventContext.STEP_TITLE to "Book course"),
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
        eventType = TimelineEventType.GOAL_UPDATED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        actionedBy = updatedGoal.lastUpdatedBy!!,
        contextualInfo = mapOf(TimelineEventContext.GOAL_TITLE to "Learn Spanish"),
      ),
      newTimelineEvent(
        sourceReference = step1Reference.toString(),
        eventType = TimelineEventType.STEP_COMPLETED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        actionedBy = updatedGoal.lastUpdatedBy!!,
        contextualInfo = mapOf(TimelineEventContext.STEP_TITLE to "Book Spanish course"),
      ),
      newTimelineEvent(
        sourceReference = step1Reference.toString(),
        eventType = TimelineEventType.STEP_UPDATED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        actionedBy = updatedGoal.lastUpdatedBy!!,
        contextualInfo = mapOf(TimelineEventContext.STEP_TITLE to "Book Spanish course"),
      ),
      newTimelineEvent(
        sourceReference = step2Reference.toString(),
        eventType = TimelineEventType.STEP_STARTED,
        prisonId = updatedGoal.lastUpdatedAtPrison,
        actionedBy = updatedGoal.lastUpdatedBy!!,
        contextualInfo = mapOf(TimelineEventContext.STEP_TITLE to "Complete course"),
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

  @Test
  fun `should create goal archived event given archived goal with an archived reason`() {
    // Given
    val goal = aValidGoal(
      status = GoalStatus.ARCHIVED,
      archiveReason = ReasonToArchiveGoal.PRISONER_NO_LONGER_WANTS_TO_WORK_TOWARDS_GOAL,
    )

    // When
    val actual = timelineEventFactory.goalArchivedTimelineEvent(goal)

    // Then
    assertThat(actual)
      .hasSourceReference(goal.reference.toString())
      .hasEventType(TimelineEventType.GOAL_ARCHIVED)
      .hasPrisonId(goal.createdAtPrison)
      .wasActionedBy(goal.lastUpdatedBy!!)
      .hasContextualInfo(
        mapOf(
          TimelineEventContext.GOAL_TITLE to goal.title,
          TimelineEventContext.GOAL_ARCHIVED_REASON to "PRISONER_NO_LONGER_WANTS_TO_WORK_TOWARDS_GOAL",
        ),
      )
  }

  @Test
  fun `should create goal archived event given archived goal with an 'OTHER' archived reason`() {
    // Given
    val goal = aValidGoal(
      status = GoalStatus.ARCHIVED,
      archiveReason = ReasonToArchiveGoal.OTHER,
      archiveReasonOther = "Prisoner has deceased",
    )

    // When
    val actual = timelineEventFactory.goalArchivedTimelineEvent(goal)

    // Then
    assertThat(actual)
      .hasSourceReference(goal.reference.toString())
      .hasEventType(TimelineEventType.GOAL_ARCHIVED)
      .hasPrisonId(goal.createdAtPrison)
      .wasActionedBy(goal.lastUpdatedBy!!)
      .hasContextualInfo(
        mapOf(
          TimelineEventContext.GOAL_TITLE to goal.title,
          TimelineEventContext.GOAL_ARCHIVED_REASON to "OTHER",
          TimelineEventContext.GOAL_ARCHIVED_REASON_OTHER to "Prisoner has deceased",
        ),
      )
  }

  @Test
  fun `should create goal un-archived event`() {
    // Given
    val goal = aValidGoal(status = GoalStatus.ACTIVE)

    // When
    val actual = timelineEventFactory.goalUnArchivedTimelineEvent(goal)

    // Then
    assertThat(actual)
      .hasSourceReference(goal.reference.toString())
      .hasEventType(TimelineEventType.GOAL_UNARCHIVED)
      .hasPrisonId(goal.createdAtPrison)
      .wasActionedBy(goal.lastUpdatedBy!!)
      .hasContextualInfo(mapOf(TimelineEventContext.GOAL_TITLE to goal.title))
  }
}
