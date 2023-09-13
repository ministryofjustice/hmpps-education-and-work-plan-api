package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.Timeline
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.aValidTimelineEvent
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class TimelineServiceTest {

  @InjectMocks
  private lateinit var service: TimelineService

  @Mock
  private lateinit var persistenceAdapter: TimelinePersistenceAdapter

  companion object {
    private const val prisonNumber = "A1234AB"

    private val earliestEvent = aValidTimelineEvent(
      eventType = TimelineEventType.ACTION_PLAN_CREATED,
      timestamp = Instant.MIN,
    )
    private val middleEvent = aValidTimelineEvent(
      eventType = TimelineEventType.GOAL_CREATED,
      timestamp = Instant.now(),
    )
    private val latestEvent = aValidTimelineEvent(
      eventType = TimelineEventType.STEP_STARTED,
      timestamp = Instant.MAX,
    )
  }

  @Test
  fun `should record timeline event`() {
    // Given
    val timelineEvent = aValidTimelineEvent()

    // When
    service.recordTimelineEvent(prisonNumber, timelineEvent)

    // Then
    verify(persistenceAdapter).recordTimelineEvent(prisonNumber, timelineEvent)
  }

  @Test
  fun `should record timeline events`() {
    // Given
    val timelineEvents = listOf(earliestEvent, middleEvent, latestEvent)

    // When
    service.recordTimelineEvents(prisonNumber, timelineEvents)

    // Then
    verify(persistenceAdapter).recordTimelineEvents(prisonNumber, timelineEvents)
  }

  @Test
  fun `should get timeline for prisoner`() {
    // Given
    given(persistenceAdapter.getTimelineEventsForPrisoner(any())).willReturn(
      listOf(
        earliestEvent,
        middleEvent,
        latestEvent,
      ),
    )

    val expected = Timeline(
      prisonNumber,
      listOf(
        earliestEvent,
        middleEvent,
        latestEvent,
      ),
    )
    // When
    val actual = service.getTimelineForPrisoner(prisonNumber)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    verify(persistenceAdapter).getTimelineEventsForPrisoner(prisonNumber)
  }
}
