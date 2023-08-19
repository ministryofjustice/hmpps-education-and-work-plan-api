package uk.gov.justice.digital.hmpps.educationandworkplanapi.timeline.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.timeline.Timeline
import uk.gov.justice.digital.hmpps.educationandworkplanapi.timeline.aValidTimelineEvent
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
      title = "The first event",
      eventDateTime = Instant.MIN,
    )
    private val middleEvent = aValidTimelineEvent(
      title = "The middle event",
      eventDateTime = Instant.now(),
    )
    private val latestEvent = aValidTimelineEvent(
      title = "The last event",
      eventDateTime = Instant.MAX,
    )
  }

  @Test
  fun `should get timeline for prisoner`() {
    // Given
    given(persistenceAdapter.getTimelineEventsForPrisoner(any())).willReturn(
      listOf(
        earliestEvent,
        middleEvent,
        latestEvent,
      ).shuffled(), // there is no guarantee what order the persistenceService will return the events in
    )

    val expected = Timeline(
      prisonNumber,
      listOf(
        latestEvent,
        middleEvent,
        earliestEvent,
      ),
    )
    // When
    val actual = service.getTimelineForPrisoner(prisonNumber)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    verify(persistenceAdapter).getTimelineEventsForPrisoner(prisonNumber)
  }
}
