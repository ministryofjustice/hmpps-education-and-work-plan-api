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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.event.AsyncGoalEventService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.event.TimelineEventResolver
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
  private lateinit var timelineEventResolver: TimelineEventResolver

  @Mock
  private lateinit var timelineService: TimelineService

  @Test
  fun `should send event details given goal created`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createdGoal = aValidGoal()

    // When
    goalEventService.goalCreated(prisonNumber = prisonNumber, createdGoal = createdGoal)

    // Then
    await.untilAsserted {
      verify(telemetryService).trackGoalCreateEvent(createdGoal)
      verify(timelineService).recordTimelineEvent(any(), any()) // TODO RR-
    }
  }

  @Test
  fun `should send event details given goal updated`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val updatedGoal = aValidGoal()
    val existingGoal = aValidGoal()
    val expectedTimelineEvents = listOf(aValidTimelineEvent())
    given(timelineEventResolver.resolveGoalUpdatedEvents(updatedGoal, existingGoal)).willReturn(expectedTimelineEvents)

    // When
    goalEventService.goalUpdated(prisonNumber = prisonNumber, updatedGoal = updatedGoal, existingGoal = existingGoal)

    // Then
    await.untilAsserted {
      verify(telemetryService).trackGoalUpdateEvent(updatedGoal)
      verify(timelineService).recordTimelineEvents(prisonNumber, expectedTimelineEvents)
    }
  }
}
