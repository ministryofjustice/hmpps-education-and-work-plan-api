package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.aValidTimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service.TimelineService

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

  @Test
  fun `should send event details given goal created`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createdGoal = aValidGoal()
    val createGoalTimelineEvent = aValidTimelineEvent()
    given(timelineEventFactory.goalCreatedTimelineEvent(any())).willReturn(createGoalTimelineEvent)

    // When
    goalEventService.goalCreated(prisonNumber = prisonNumber, createdGoal = createdGoal)

    // Then
    await.untilAsserted {
      verify(timelineEventFactory).goalCreatedTimelineEvent(createdGoal)
      verify(telemetryService).trackGoalCreatedEvent(createdGoal)
      verify(timelineService).recordTimelineEvent(prisonNumber, createGoalTimelineEvent)
    }
  }

  @Test
  fun `should send event details given goal updated`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
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
}
