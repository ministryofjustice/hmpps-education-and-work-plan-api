package uk.gov.justice.digital.hmpps.educationandworkplanapi.timeline

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class TimelineTest {

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
  fun `should create timeline given events out of sequence`() {
    // Given
    val events = listOf(middleEvent, earliestEvent, latestEvent)

    // When
    val actual = Timeline(prisonNumber, events)

    // Then
    assertThat(actual.events).containsExactly(
      latestEvent,
      middleEvent,
      earliestEvent,
    )
  }

  @Test
  fun `should add event and maintain event order`() {
    // Given
    val initialEvents = listOf(earliestEvent, latestEvent)
    val timeline = Timeline(prisonNumber, initialEvents)

    // When
    timeline.addEvent(middleEvent)

    // Then
    assertThat(timeline.events).containsExactly(
      latestEvent,
      middleEvent,
      earliestEvent,
    )
  }
}
