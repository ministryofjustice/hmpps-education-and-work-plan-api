package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.timeline

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
import uk.gov.justice.digital.hmpps.domain.timeline.aValidTimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.aValidTimelineEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.aValidTimelineEventEntity
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class TimelineEntityMapperTest {

  @InjectMocks
  private lateinit var mapper: TimelineEntityMapper

  @Mock
  private lateinit var eventMapper: TimelineEventEntityMapper

  @Test
  fun `should map from entity to domain`() {
    // Given
    val reference = UUID.randomUUID()
    val prisonNumber = randomValidPrisonNumber()
    val timelineEntity = aValidTimelineEntity(
      reference = reference,
      prisonNumber = prisonNumber,
      events = mutableListOf(aValidTimelineEventEntity()),
    )
    val timelineEvent = aValidTimelineEvent()
    val expectedEvents = listOf(timelineEvent)
    val expected = aValidTimeline(
      reference = reference,
      prisonNumber = prisonNumber,
      events = expectedEvents,
    )
    given(eventMapper.fromEntityToDomain(any())).willReturn(timelineEvent)

    // When
    val actual = mapper.fromEntityToDomain(timelineEntity)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
  }
}
