package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.PrisonApiClient
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent

@ExtendWith(MockitoExtension::class)
class PrisonerApiTimelineServiceTest {

  @InjectMocks
  private lateinit var prisonerApiTimelineService: PrisonerApiTimelineService

  @Mock
  private lateinit var prisonApiClient: PrisonApiClient

  @Test
  fun `should get Prison timeline events`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    // TODO RR-580 populate and map PrisonMovementEvents
    // given(prisonApiClient.getPrisonMovementEvents(any())).willReturn(PrisonMovementEvents(prisonNumber, emptyMap()))
    val expected = emptyList<TimelineEvent>()

    // When
    val actual = prisonerApiTimelineService.getPrisonTimelineEvents(prisonNumber)

    // Then
    assertThat(actual).isEqualTo(expected)
    // verify(prisonApiClient).getPrisonMovementEvents(prisonNumber)
  }
}
