package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.timeline

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext
import uk.gov.justice.digital.hmpps.domain.timeline.aValidTimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventResponse
import java.time.OffsetDateTime
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType as TimelineEventTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType as TimelineEventTypeModel

@ExtendWith(MockitoExtension::class)
class TimelineEventResourceMapperTest {
  @InjectMocks
  private lateinit var mapper: TimelineEventResourceMapperImpl

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Test
  fun `should map from domain to model`() {
    // Given
    val reference = UUID.randomUUID()
    val sourceReference = UUID.randomUUID().toString()
    val goalTitle = "Learn French"
    val prisonId = "BXI"
    val actionedBy = "asmith_gen"
    val actionedByDisplayName = "Alex Smith"
    val timestamp = OffsetDateTime.now()
    val correlationId = UUID.randomUUID()
    val timelineEventDomain = aValidTimelineEvent(
      reference = reference,
      sourceReference = sourceReference,
      eventType = TimelineEventTypeDomain.GOAL_CREATED,
      contextualInfo = mapOf(TimelineEventContext.GOAL_TITLE to goalTitle),
      prisonId = prisonId,
      actionedBy = actionedBy,
      actionedByDisplayName = actionedByDisplayName,
      correlationId = correlationId,
    )
    val expected = TimelineEventResponse(
      reference = reference,
      sourceReference = sourceReference,
      eventType = TimelineEventTypeModel.GOAL_CREATED,
      contextualInfo = mapOf("GOAL_TITLE" to goalTitle),
      prisonId = prisonId,
      actionedBy = actionedBy,
      actionedByDisplayName = actionedByDisplayName,
      timestamp = timestamp,
      correlationId = correlationId,
    )
    given(instantMapper.toOffsetDateTime(any())).willReturn(timestamp)

    // When
    val actual = mapper.fromDomainToModel(timelineEventDomain)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
