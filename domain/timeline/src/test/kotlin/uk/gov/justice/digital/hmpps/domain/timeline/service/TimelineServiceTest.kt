package uk.gov.justice.digital.hmpps.domain.timeline.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.timeline.Timeline
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineNotFoundException
import uk.gov.justice.digital.hmpps.domain.timeline.aValidPrisonMovementTimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.aValidTimeline
import uk.gov.justice.digital.hmpps.domain.timeline.aValidTimelineEvent
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class TimelineServiceTest {

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
  fun `should record timeline event`() {
    // Given
    val timelineEvent = aValidTimelineEvent()

    // When
    service.recordTimelineEvent(PRISON_NUMBER, timelineEvent)

    // Then
    verify(persistenceAdapter).recordTimelineEvent(PRISON_NUMBER, timelineEvent)
  }

  @Test
  fun `should record timeline events`() {
    // Given
    val timelineEvents = listOf(aValidTimelineEvent(), aValidTimelineEvent())

    // When
    service.recordTimelineEvents(PRISON_NUMBER, timelineEvents)

    // Then
    verify(persistenceAdapter).recordTimelineEvents(PRISON_NUMBER, timelineEvents)
  }

  @Test
  fun `should get timeline for prisoner`() {
    // Given
    val expectedTimeline = aValidTimeline()
    val prisonEvents = listOf(aValidPrisonMovementTimelineEvent())
    given(persistenceAdapter.getTimelineForPrisoner(any())).willReturn(expectedTimeline)
    given(prisonTimelineService.getPrisonTimelineEvents(any())).willReturn(prisonEvents)
    expectedTimeline.addEvents(prisonEvents)

    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER)

    // Then
    assertThat(actual).isEqualTo(expectedTimeline)
    verify(persistenceAdapter).getTimelineForPrisoner(PRISON_NUMBER)
    verify(prisonTimelineService).getPrisonTimelineEvents(PRISON_NUMBER)
  }

  @Test
  fun `should get timeline for prisoner with no induction or goals`() {
    // Given
    val plpTimeline = null
    val prisonEvents = listOf(aValidPrisonMovementTimelineEvent())
    given(persistenceAdapter.getTimelineForPrisoner(any())).willReturn(plpTimeline)
    given(prisonTimelineService.getPrisonTimelineEvents(any())).willReturn(prisonEvents)
    val expectedTimeline = Timeline(UUID.randomUUID(), PRISON_NUMBER, prisonEvents)

    // When
    val actual = service.getTimelineForPrisoner(PRISON_NUMBER)

    // Then
    assertThat(actual).usingRecursiveComparison().ignoringFields("reference").isEqualTo(expectedTimeline)
    verify(persistenceAdapter).getTimelineForPrisoner(PRISON_NUMBER)
    verify(prisonTimelineService).getPrisonTimelineEvents(PRISON_NUMBER)
  }

  @Test
  fun `should fail to get timeline for prisoner given timeline does not exist`() {
    // Given
    given(persistenceAdapter.getTimelineForPrisoner(any())).willReturn(null)
    given(prisonTimelineService.getPrisonTimelineEvents(any())).willReturn(emptyList())

    // When
    val exception =
      catchThrowableOfType(TimelineNotFoundException::class.java) { service.getTimelineForPrisoner(PRISON_NUMBER) }

    // Then
    assertThat(exception).hasMessage("Timeline not found for prisoner [$PRISON_NUMBER]")
    assertThat(exception.prisonNumber).isEqualTo(PRISON_NUMBER)
    verify(persistenceAdapter).getTimelineForPrisoner(PRISON_NUMBER)
    verify(prisonTimelineService).getPrisonTimelineEvents(PRISON_NUMBER)
  }
}
