package uk.gov.justice.digital.hmpps.domain.timeline

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class TimelineTest {

  companion object {
    private const val PRISON_NUMBER = "A1234AB"

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
  fun `should create timeline given events out of sequence`() {
    // Given
    val reference = UUID.randomUUID()
    val events = listOf(middleEvent, earliestEvent, latestEvent)

    // When
    val actual = Timeline(reference = reference, prisonNumber = PRISON_NUMBER, events = events)

    // Then
    assertThat(actual.events).containsExactly(
      earliestEvent,
      middleEvent,
      latestEvent,
    )
  }

  @Test
  fun `should add event and maintain event order`() {
    // Given
    val reference = UUID.randomUUID()
    val initialEvents = listOf(earliestEvent, latestEvent)
    val timeline = Timeline(reference = reference, prisonNumber = PRISON_NUMBER, events = initialEvents)

    // When
    timeline.addEvent(middleEvent)

    // Then
    assertThat(timeline.events).containsExactly(
      earliestEvent,
      middleEvent,
      latestEvent,
    )
  }
}
