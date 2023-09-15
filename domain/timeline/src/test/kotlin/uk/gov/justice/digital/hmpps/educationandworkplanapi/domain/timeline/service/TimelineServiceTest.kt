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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineNotFoundException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.aValidTimeline
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.aValidTimelineEvent

@ExtendWith(MockitoExtension::class)
class TimelineServiceTest {

  @InjectMocks
  private lateinit var service: TimelineService

  @Mock
  private lateinit var persistenceAdapter: TimelinePersistenceAdapter

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
    val expected = aValidTimeline()
    given(persistenceAdapter.getTimelineForPrisoner(any())).willReturn(expected)

    // When
    val actual = service.getTimelineForPrisoner(prisonNumber)

    // Then
    assertThat(actual).isEqualTo(expected)
    verify(persistenceAdapter).getTimelineForPrisoner(prisonNumber)
  }

  @Test
  fun `should fail to get timeline for prisoner given timeline does not exist`() {
    // Given
    val prisonNumber = "A1234AB"
    given(persistenceAdapter.getTimelineForPrisoner(any())).willReturn(null)

    // When
    val exception = catchThrowableOfType(
      { service.getTimelineForPrisoner(Companion.prisonNumber) },
      TimelineNotFoundException::class.java,
    )

    // Then
    assertThat(exception).hasMessage("Timeline not found for prisoner [$prisonNumber]")
    assertThat(exception.prisonNumber).isEqualTo(prisonNumber)
    verify(persistenceAdapter).getTimelineForPrisoner(prisonNumber)
  }
}
