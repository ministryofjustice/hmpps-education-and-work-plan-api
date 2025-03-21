package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
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
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidGoal
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.timeline.aValidTimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class AsyncGoalEventServiceTest {

  @InjectMocks
  private lateinit var goalEventService: AsyncGoalEventService

  @Mock
  private lateinit var telemetryService: TelemetryService

  @Mock
  private lateinit var timelineEventFactory: TimelineEventFactory

  @Mock
  private lateinit var timelineService: TimelineService

  @Captor
  private lateinit var correlationIdCaptor: ArgumentCaptor<UUID>

  @Test
  fun `should send event details given single goal created`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val createdGoal = aValidGoal()
    val createGoalTimelineEvent = aValidTimelineEvent()
    given(timelineEventFactory.goalCreatedTimelineEvent(any(), any())).willReturn(createGoalTimelineEvent)

    val createdGoals = listOf(createdGoal)

    // When
    goalEventService.goalsCreated(prisonNumber = prisonNumber, createdGoals = createdGoals)

    // Then
    await.untilAsserted {
      verify(timelineEventFactory).goalCreatedTimelineEvent(eq(createdGoal), any())
      verify(telemetryService).trackGoalCreatedEvent(eq(createdGoal), any())
      verify(timelineService).recordTimelineEvent(prisonNumber, createGoalTimelineEvent)
    }
  }

  @Test
  fun `should send event details given multiple goals created`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()

    val createdGoal1 = aValidGoal()
    val createGoal1TimelineEvent = aValidTimelineEvent()
    val createdGoal2 = aValidGoal()
    val createGoal2TimelineEvent = aValidTimelineEvent()
    given(timelineEventFactory.goalCreatedTimelineEvent(any(), any())).willReturn(createGoal1TimelineEvent, createGoal2TimelineEvent)

    val createdGoals = listOf(createdGoal1, createdGoal2)

    // When
    goalEventService.goalsCreated(prisonNumber = prisonNumber, createdGoals = createdGoals)

    // Then
    await.untilAsserted {
      verify(timelineEventFactory).goalCreatedTimelineEvent(eq(createdGoal1), capture(correlationIdCaptor))
      verify(timelineEventFactory).goalCreatedTimelineEvent(eq(createdGoal2), capture(correlationIdCaptor))
      val correlationIdForTimelineEvent1 = correlationIdCaptor.firstValue
      val correlationIdForTimelineEvent2 = correlationIdCaptor.secondValue
      assertThat(correlationIdForTimelineEvent1).isEqualTo(correlationIdForTimelineEvent2)

      verify(telemetryService).trackGoalCreatedEvent(eq(createdGoal1), any())
      verify(telemetryService).trackGoalCreatedEvent(eq(createdGoal2), any())
      verify(timelineService).recordTimelineEvent(prisonNumber, createGoal1TimelineEvent)
      verify(timelineService).recordTimelineEvent(prisonNumber, createGoal2TimelineEvent)
    }
  }

  @Test
  fun `should send event details given goal updated`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val previousGoal = aValidGoal()
    val updatedGoal = aValidGoal()
    val expectedTimelineEvents = listOf(aValidTimelineEvent())
    given(timelineEventFactory.goalUpdatedEvents(any(), any())).willReturn(expectedTimelineEvents)

    // When
    goalEventService.goalUpdated(prisonNumber = prisonNumber, previousGoal = previousGoal, updatedGoal = updatedGoal)

    // Then
    await.untilAsserted {
      verify(timelineEventFactory).goalUpdatedEvents(previousGoal, updatedGoal)
      verify(timelineService).recordTimelineEvents(prisonNumber, expectedTimelineEvents)
      verify(telemetryService).trackGoalUpdatedEvents(previousGoal, updatedGoal)
    }
  }

  @Test
  fun `should send event details given goal archived`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val archivedGoal = aValidGoal(status = GoalStatus.ARCHIVED)
    val expectedTimelineEvent = aValidTimelineEvent()
    given(timelineEventFactory.goalArchivedTimelineEvent(any(), any())).willReturn(expectedTimelineEvent)

    // When
    goalEventService.goalArchived(prisonNumber = prisonNumber, archivedGoal = archivedGoal)

    // Then
    await.untilAsserted {
      verify(timelineEventFactory).goalArchivedTimelineEvent(eq(archivedGoal), any())
      verify(timelineService).recordTimelineEvent(prisonNumber, expectedTimelineEvent)
      verify(telemetryService).trackGoalArchivedEvent(eq(archivedGoal), any())
    }
  }

  @Test
  fun `should send event details given goal unarchived`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val unArchivedGoal = aValidGoal(status = GoalStatus.ARCHIVED)
    val expectedTimelineEvent = aValidTimelineEvent()
    given(timelineEventFactory.goalUnArchivedTimelineEvent(any(), any())).willReturn(expectedTimelineEvent)

    // When
    goalEventService.goalUnArchived(prisonNumber = prisonNumber, unArchivedGoal = unArchivedGoal)

    // Then
    await.untilAsserted {
      verify(timelineEventFactory).goalUnArchivedTimelineEvent(eq(unArchivedGoal), any())
      verify(timelineService).recordTimelineEvent(prisonNumber, expectedTimelineEvent)
      verify(telemetryService).trackGoalUnArchivedEvent(eq(unArchivedGoal), any())
    }
  }
}
