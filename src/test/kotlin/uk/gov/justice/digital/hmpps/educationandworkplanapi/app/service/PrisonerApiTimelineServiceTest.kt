package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.PrisonApiClient
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.PrisonApiException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.aValidPrisonMovementEvents
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.mapper.PrisonMovementEventsMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.aValidPrisonMovementTimelineEvent

@ExtendWith(MockitoExtension::class)
class PrisonerApiTimelineServiceTest {

  @InjectMocks
  private lateinit var prisonerApiTimelineService: PrisonerApiTimelineService

  @Mock
  private lateinit var prisonApiClient: PrisonApiClient

  @Mock
  private lateinit var prisonMovementsMapper: PrisonMovementEventsMapper

  @Test
  fun `should get Prison timeline events`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val prisonMovementEvents = aValidPrisonMovementEvents()
    val expected = listOf(aValidPrisonMovementTimelineEvent())
    given(prisonApiClient.getPrisonMovementEvents(any())).willReturn(prisonMovementEvents)
    given(prisonMovementsMapper.toTimelineEvents(any())).willReturn(expected)

    // When
    val actual = prisonerApiTimelineService.getPrisonTimelineEvents(prisonNumber)

    // Then
    assertThat(actual).isEqualTo(expected)
    verify(prisonApiClient).getPrisonMovementEvents(prisonNumber)
    verify(prisonMovementsMapper).toTimelineEvents(prisonMovementEvents)
  }

  @Test
  fun `should fail to get Prison timeline events given prison-api is unavailable`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    given(prisonApiClient.getPrisonMovementEvents(any())).willThrow(
      PrisonApiException("Error retrieving prison history for Prisoner $prisonNumber", RuntimeException()),
    )
    val expected = emptyList<TimelineEvent>()

    // When
    val actual = prisonerApiTimelineService.getPrisonTimelineEvents(prisonNumber)

    // Then
    assertThat(actual).isEqualTo(expected)
    verify(prisonApiClient).getPrisonMovementEvents(prisonNumber)
  }
}
