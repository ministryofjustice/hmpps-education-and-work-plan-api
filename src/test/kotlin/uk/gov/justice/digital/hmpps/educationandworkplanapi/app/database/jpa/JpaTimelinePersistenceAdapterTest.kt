package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.timeline.aValidTimeline
import uk.gov.justice.digital.hmpps.domain.timeline.aValidTimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.TimelineEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.aValidTimelineEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.aValidTimelineEventEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.timeline.TimelineEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.timeline.TimelineEventEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.TimelineRepository

@ExtendWith(MockitoExtension::class)
class JpaTimelinePersistenceAdapterTest {

  @InjectMocks
  private lateinit var persistenceAdapter: JpaTimelinePersistenceAdapter

  @Mock
  private lateinit var timelineRepository: TimelineRepository

  @Mock
  private lateinit var timelineMapper: TimelineEntityMapper

  @Mock
  private lateinit var timelineEventMapper: TimelineEventEntityMapper

  @Captor
  private lateinit var timelineEntityCaptor: ArgumentCaptor<TimelineEntity>

  @Nested
  inner class AddTimelineEvent {

    @Test
    fun `should add timeline event to new timeline given timeline does not exist`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val timelineEvent = aValidTimelineEvent()
      val timelineEventEntity = aValidTimelineEventEntity()
      given(timelineRepository.findByPrisonNumber(any())).willReturn(null)
      given(timelineEventMapper.fromDomainToEntity(any())).willReturn(timelineEventEntity)
      given(timelineEventMapper.fromEntityToDomain(any())).willReturn(timelineEvent)

      // When
      persistenceAdapter.recordTimelineEvent(prisonNumber = prisonNumber, event = timelineEvent)

      // Then
      verify(timelineRepository).findByPrisonNumber(prisonNumber)
      verify(timelineEventMapper).fromDomainToEntity(timelineEvent)
      verify(timelineEventMapper).fromEntityToDomain(timelineEventEntity)
      verify(timelineRepository).saveAndFlush(capture(timelineEntityCaptor))
    }

    @Test
    fun `should record timeline event given timeline already exists`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val timelineEvent = aValidTimelineEvent()
      val timelineEntity = aValidTimelineEntity()
      val timelineEventEntity = aValidTimelineEventEntity()
      given(timelineRepository.findByPrisonNumber(any())).willReturn(timelineEntity)
      given(timelineEventMapper.fromDomainToEntity(any())).willReturn(timelineEventEntity)
      given(timelineEventMapper.fromEntityToDomain(any())).willReturn(timelineEvent)

      // When
      persistenceAdapter.recordTimelineEvent(prisonNumber = prisonNumber, event = timelineEvent)

      // Then
      verify(timelineRepository).findByPrisonNumber(prisonNumber)
      verify(timelineEventMapper).fromDomainToEntity(timelineEvent)
      verify(timelineEventMapper).fromEntityToDomain(timelineEventEntity)
      verify(timelineRepository).saveAndFlush(timelineEntity)
    }
  }

  @Nested
  inner class AddTimelineEvents {
    @Test
    fun `should add timeline events to new timeline given timeline does not exist`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val timelineEvent1 = aValidTimelineEvent()
      val timelineEvent2 = aValidTimelineEvent()
      val timelineEvents = listOf(timelineEvent1, timelineEvent2)
      val timeline = aValidTimeline(prisonNumber = prisonNumber, events = timelineEvents)
      val timelineEventEntity1 = aValidTimelineEventEntity()
      val timelineEventEntity2 = aValidTimelineEventEntity()
      given(timelineRepository.findByPrisonNumber(any())).willReturn(null)
      given(timelineEventMapper.fromDomainToEntity(timelineEvent1)).willReturn(timelineEventEntity1)
      given(timelineEventMapper.fromDomainToEntity(timelineEvent2)).willReturn(timelineEventEntity2)
      given(timelineMapper.fromEntityToDomain(any())).willReturn(timeline)

      // When
      persistenceAdapter.recordTimelineEvents(prisonNumber = prisonNumber, events = timelineEvents)

      // Then
      verify(timelineRepository).findByPrisonNumber(prisonNumber)
      verify(timelineEventMapper).fromDomainToEntity(timelineEvent1)
      verify(timelineEventMapper).fromDomainToEntity(timelineEvent2)
      verify(timelineMapper).fromEntityToDomain(capture(timelineEntityCaptor))
      verify(timelineRepository).saveAndFlush(capture(timelineEntityCaptor))
    }

    @Test
    fun `should record timeline events given timeline already exists`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val timelineEvent1 = aValidTimelineEvent()
      val timelineEvent2 = aValidTimelineEvent()
      val timelineEvents = listOf(timelineEvent1, timelineEvent2)
      val timeline = aValidTimeline(prisonNumber = prisonNumber, events = timelineEvents)
      val timelineEventEntity1 = aValidTimelineEventEntity()
      val timelineEventEntity2 = aValidTimelineEventEntity()
      val timelineEntity = aValidTimelineEntity(events = mutableListOf(timelineEventEntity1, timelineEventEntity2))
      given(timelineRepository.findByPrisonNumber(any())).willReturn(timelineEntity)
      given(timelineEventMapper.fromDomainToEntity(timelineEvent1)).willReturn(timelineEventEntity1)
      given(timelineEventMapper.fromDomainToEntity(timelineEvent2)).willReturn(timelineEventEntity2)
      given(timelineMapper.fromEntityToDomain(any())).willReturn(timeline)

      // When
      persistenceAdapter.recordTimelineEvents(prisonNumber = prisonNumber, events = timelineEvents)

      // Then
      verify(timelineRepository).findByPrisonNumber(prisonNumber)
      verify(timelineEventMapper).fromDomainToEntity(timelineEvent1)
      verify(timelineEventMapper).fromDomainToEntity(timelineEvent2)
      verify(timelineMapper).fromEntityToDomain(timelineEntity)
      verify(timelineRepository).saveAndFlush(timelineEntity)
    }
  }

  @Nested
  inner class GetTimeline {
    @Test
    fun `should get timeline for prisoner`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val timeline = aValidTimeline()
      val timelineEntity = aValidTimelineEntity()
      given(timelineRepository.findByPrisonNumber(any())).willReturn(timelineEntity)
      given(timelineMapper.fromEntityToDomain(any())).willReturn(timeline)

      // When
      val actual = persistenceAdapter.getTimelineForPrisoner(prisonNumber)

      // Then
      assertThat(actual).isEqualTo(timeline)
      verify(timelineRepository).findByPrisonNumber(prisonNumber)
      verify(timelineMapper).fromEntityToDomain(timelineEntity)
    }

    @Test
    fun `should not get timeline given timeline does not exist for prisoner`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      given(timelineRepository.findByPrisonNumber(any())).willReturn(null)

      // When
      val actual = persistenceAdapter.getTimelineForPrisoner(prisonNumber)

      // Then
      assertThat(actual).isNull()
      verifyNoInteractions(timelineMapper)
    }
  }
}
