package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service

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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.Timeline
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineNotFoundException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.aValidPrisonMovementTimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.aValidTimeline
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.aValidTimelineEvent
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
    private const val prisonNumber = "A1234AB"
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
    val timelineEvents = listOf(aValidTimelineEvent(), aValidTimelineEvent())

    // When
    service.recordTimelineEvents(prisonNumber, timelineEvents)

    // Then
    verify(persistenceAdapter).recordTimelineEvents(prisonNumber, timelineEvents)
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
    val actual = service.getTimelineForPrisoner(prisonNumber)

    // Then
    assertThat(actual).isEqualTo(expectedTimeline)
    verify(persistenceAdapter).getTimelineForPrisoner(prisonNumber)
    verify(prisonTimelineService).getPrisonTimelineEvents(prisonNumber)
  }

  @Test
  fun `should get timeline for prisoner with no induction or goals`() {
    // Given
    val plpTimeline = null
    val prisonEvents = listOf(aValidPrisonMovementTimelineEvent())
    given(persistenceAdapter.getTimelineForPrisoner(any())).willReturn(plpTimeline)
    given(prisonTimelineService.getPrisonTimelineEvents(any())).willReturn(prisonEvents)
    val expectedTimeline = Timeline(UUID.randomUUID(), prisonNumber, prisonEvents)

    // When
    val actual = service.getTimelineForPrisoner(prisonNumber)

    // Then
    assertThat(actual).usingRecursiveComparison().ignoringFields("reference").isEqualTo(expectedTimeline)
    verify(persistenceAdapter).getTimelineForPrisoner(prisonNumber)
    verify(prisonTimelineService).getPrisonTimelineEvents(prisonNumber)
  }

  @Test
  fun `should fail to get timeline for prisoner given timeline does not exist`() {
    // Given
    given(persistenceAdapter.getTimelineForPrisoner(any())).willReturn(null)
    given(prisonTimelineService.getPrisonTimelineEvents(any())).willReturn(emptyList())

    // When
    val exception = catchThrowableOfType(
      { service.getTimelineForPrisoner(prisonNumber) },
      TimelineNotFoundException::class.java,
    )

    // Then
    assertThat(exception).hasMessage("Timeline not found for prisoner [$prisonNumber]")
    assertThat(exception.prisonNumber).isEqualTo(prisonNumber)
    verify(persistenceAdapter).getTimelineForPrisoner(prisonNumber)
    verify(prisonTimelineService).getPrisonTimelineEvents(prisonNumber)
  }
}
