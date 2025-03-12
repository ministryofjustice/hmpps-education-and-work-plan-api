package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.timeline

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.timeline.aValidTimeline
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.timeline.aValidTimelineEventResponse
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class TimelineResourceMapperTest {
  @InjectMocks
  private lateinit var mapper: TimelineResourceMapper

  @Mock
  private lateinit var timelineEventMapper: TimelineEventResourceMapper

  @Test
  fun `should map from domain to model given user is successfully looked up`() {
    // Given
    val reference = UUID.randomUUID()
    val prisonNumber = randomValidPrisonNumber()
    val timelineDomain = aValidTimeline(reference = reference, prisonNumber = prisonNumber)
    val timelineEventResponse = aValidTimelineEventResponse()
    val expected = TimelineResponse(
      reference = reference,
      prisonNumber = prisonNumber,
      events = listOf(timelineEventResponse),
    )
    given(timelineEventMapper.fromDomainToModel(any())).willReturn(timelineEventResponse)

    // When
    val actual = mapper.fromDomainToModel(timelineDomain)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
