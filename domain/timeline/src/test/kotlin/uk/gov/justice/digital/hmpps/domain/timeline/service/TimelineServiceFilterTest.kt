package uk.gov.justice.digital.hmpps.domain.timeline.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.domain.timeline.aValidTimeline
import uk.gov.justice.digital.hmpps.domain.timeline.aValidTimelineEvent
import java.time.LocalDate
import java.time.ZoneId

@ExtendWith(MockitoExtension::class)
class TimelineServiceFilterTest {

  @InjectMocks
  private lateinit var service: TimelineService

  @Mock
  private lateinit var persistenceAdapter: TimelinePersistenceAdapter

  @Mock
  private lateinit var prisonTimelineService: PrisonTimelineService

  companion object {
    private const val PRISON_NUMBER = "A1234AB"
  }

  @Test
  fun `should get timeline events for prisoner`() {
    // Given
    setUpEvents()
    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER)
    // Then
    assertThat(actual.events.size).isEqualTo(42)
  }

  @Test
  fun `should get timeline review events for prisoner`() {
    // Given
    setUpEvents()
    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER, reviews = true)
    // Then
    assertThat(actual.events.size).isEqualTo(6)
  }

  @Test
  fun `should get timeline review events for prisoner when all flags are false`() {
    // Given
    setUpEvents()
    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER, reviews = false, inductions = false, prisonEvents = false, goals = false)
    // Then
    assertThat(actual.events.size).isEqualTo(42)
  }

  @Test
  fun `should get timeline review events for prisoner for the last six months`() {
    // Given
    setUpEvents()
    // When
    val actual =
      service.getTimelineForPrisoner(PRISON_NUMBER, reviews = true, eventsSince = LocalDate.now().minusMonths(6))
    // Then
    assertThat(actual.events.size).isEqualTo(3)
  }

  @Test
  fun `should get timeline induction events for prisoner for the last six months`() {
    // Given
    setUpEvents()
    // When
    val actual =
      service.getTimelineForPrisoner(PRISON_NUMBER, inductions = true, eventsSince = LocalDate.now().minusMonths(6))
    // Then
    assertThat(actual.events.size).isEqualTo(6)
  }

  @Test
  fun `should get timeline goal events for prisoner for the last six months`() {
    // Given
    setUpEvents()
    // When
    val actual =
      service.getTimelineForPrisoner(PRISON_NUMBER, goals = true, eventsSince = LocalDate.now().minusMonths(6))
    // Then
    assertThat(actual.events.size).isEqualTo(10)
  }

  @Test
  fun `should get timeline review events for prisoner for the prison 1`() {
    // Given
    setUpEvents()
    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER, reviews = true, prisonId = "PRISON1")
    // Then
    assertThat(actual.events.size).isEqualTo(3)
  }

  @Test
  fun `should get timeline review events for prisoner for the prison 2`() {
    // Given
    setUpEvents()
    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER, prisonId = "PRISON2")
    // Then
    assertThat(actual.events.size).isEqualTo(21)
  }

  @Test
  fun `should get zero timeline review events for prisoner for the prison 3`() {
    // Given
    setUpEvents()
    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER, prisonId = "PRISON3")
    // Then
    assertThat(actual.events.size).isEqualTo(0)
  }

  @Test
  fun `should get zero timeline review events for prisoner when the date is in the future`() {
    // Given
    setUpEvents()
    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER, reviews = true, eventsSince = LocalDate.now().plusDays(1))
    // Then
    assertThat(actual.events.size).isEqualTo(0)
  }

  @Test
  fun `should get timeline induction events for prisoner`() {
    // Given
    setUpEvents()
    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER, inductions = true, goals = false)
    // Then
    assertThat(actual.events.size).isEqualTo(12)
  }

  @Test
  fun `should get timeline goal events for prisoner`() {
    // Given
    setUpEvents()
    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER, goals = true)
    // Then
    assertThat(actual.events.size).isEqualTo(20)
  }

  @Test
  fun `should get timeline prison events for prisoner`() {
    // Given
    setUpEvents()
    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER, prisonEvents = true)
    // Then
    assertThat(actual.events.size).isEqualTo(6)
  }

  @Test
  fun `should get timeline goal, induction and review events for prisoner`() {
    // Given
    setUpEvents()
    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER, reviews = true, goals = true, inductions = true)
    // Then
    assertThat(actual.events.size).isEqualTo(36)
  }

  private fun setUpEvents() {
    val events = twoOfEachEventType()
    val expectedTimeline = aValidTimeline(events = events)
    given(persistenceAdapter.getTimelineForPrisoner(any())).willReturn(expectedTimeline)
  }

  private fun twoOfEachEventType(): List<TimelineEvent> {
    val oldEventTimestamp = LocalDate.now().minusMonths(7).atStartOfDay(ZoneId.systemDefault()).toInstant()

    return TimelineEventType.entries.map { eventType ->
      aValidTimelineEvent(eventType = eventType, prisonId = "PRISON1")
    } +
      TimelineEventType.entries.map { eventType ->
        aValidTimelineEvent(eventType = eventType, prisonId = "PRISON2", timestamp = oldEventTimestamp)
      }
  }
}
