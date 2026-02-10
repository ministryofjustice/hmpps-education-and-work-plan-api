package uk.gov.justice.digital.hmpps.domain.timeline.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
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

  @BeforeEach
  fun setupTimelineEvents() {
    setUpEvents()
    // A total of 44 events are created:
    //  - 5 Induction events
    //  - 9 Goal events
    //  - 1 event relating to Goal and Inductions
    //  - 3 Review events
    //  - 3 Prison Movement events
    //  - 1 Employability skill event
    // The 22 events are created for both PRISON1 and PRISON2, giving a total of 44 events
  }

  @Test
  fun `should get all timeline events given no filtering`() {
    // Given

    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER)
    // Then
    assertThat(actual.events.size).isEqualTo(44)
  }

  @Test
  fun `should get 6 timeline events given filtering for reviews only`() {
    // Given

    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER, reviews = true)
    // Then
    assertThat(actual.events.size).isEqualTo(6)
  }

  @Test
  fun `should get 3 timeline events given filtering for reviews in the last six months`() {
    // Given

    // When
    val actual =
      service.getTimelineForPrisoner(PRISON_NUMBER, reviews = true, eventsSince = LocalDate.now().minusMonths(6))
    // Then
    assertThat(actual.events.size).isEqualTo(3)
  }

  @Test
  fun `should get 6 timeline events given filtering for inductions in the last six months`() {
    // Given

    // When
    val actual =
      service.getTimelineForPrisoner(PRISON_NUMBER, inductions = true, eventsSince = LocalDate.now().minusMonths(6))
    // Then
    assertThat(actual.events.size).isEqualTo(6)
  }

  @Test
  fun `should get 10 timeline events given filtering for goals in the last six months`() {
    // Given

    // When
    val actual =
      service.getTimelineForPrisoner(PRISON_NUMBER, goals = true, eventsSince = LocalDate.now().minusMonths(6))
    // Then
    assertThat(actual.events.size).isEqualTo(10)
  }

  @Test
  fun `should get 3 timeline =events given filtering for reviews in prison 1`() {
    // Given

    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER, reviews = true, prisonId = "PRISON1")
    // Then
    assertThat(actual.events.size).isEqualTo(3)
  }

  @Test
  fun `should get 22 timeline events given filtering for prison 2 only`() {
    // Given

    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER, prisonId = "PRISON2")
    // Then
    assertThat(actual.events.size).isEqualTo(22)
  }

  @Test
  fun `should get zero timeline review events given filtering for an unmatched prison`() {
    // Given

    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER, prisonId = "PRISON3")
    // Then
    assertThat(actual.events.size).isEqualTo(0)
  }

  @Test
  fun `should get zero timeline events given filtering for goals and a date in the future`() {
    // Given

    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER, reviews = true, eventsSince = LocalDate.now().plusDays(1))
    // Then
    assertThat(actual.events.size).isEqualTo(0)
  }

  @Test
  fun `should get 12 timeline events given filtering for inductions only`() {
    // Given

    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER, inductions = true, goals = false)
    // Then
    assertThat(actual.events.size).isEqualTo(12)
  }

  @Test
  fun `should get 20 timeline events given filtering for goals only`() {
    // Given

    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER, goals = true)
    // Then
    assertThat(actual.events.size).isEqualTo(20)
  }

  @Test
  fun `should get 6 timeline events given filtering for prison events only`() {
    // Given

    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER, prisonEvents = true)
    // Then
    assertThat(actual.events.size).isEqualTo(6)
  }

  @Test
  fun `should get 36 timeline events given filtering for goals, inductions and reviews`() {
    // Given

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
