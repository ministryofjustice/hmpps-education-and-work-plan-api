package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import ch.qos.logback.classic.Level
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.TestLogAppender
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service.TimelineService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.service.events.aValidCiagInductionCreatedEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.service.events.aValidCiagInductionUpdatedEvent
import java.time.Instant
import java.util.UUID

@ExtendWith(MockitoExtension::class)
internal class InboundEventsServiceTest {

  @InjectMocks
  private lateinit var inboundEventsService: InboundEventsService

  @Mock
  private lateinit var timelineService: TimelineService

  @Captor
  private lateinit var timelineEventCaptor: ArgumentCaptor<TimelineEvent>

  @Test
  fun `should process CIAG induction created event`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val inductionCreatedEvent = aValidCiagInductionCreatedEvent()

    // When
    inboundEventsService.process(inductionCreatedEvent)

    // Then
    verify(timelineService).recordTimelineEvent(eq(prisonNumber), capture(timelineEventCaptor))
    assertThat(timelineEventCaptor.value.sourceReference).isEqualTo(inductionCreatedEvent.reference())
    assertThat(timelineEventCaptor.value.eventType).isEqualTo(TimelineEventType.INDUCTION_CREATED)
    assertThat(timelineEventCaptor.value.prisonId).isEqualTo(inductionCreatedEvent.prisonId())
    assertThat(timelineEventCaptor.value.actionedBy).isEqualTo(inductionCreatedEvent.userId())
  }

  @Test
  fun `should process CIAG induction updated event`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val inductionUpdatedEvent = aValidCiagInductionUpdatedEvent()

    // When
    inboundEventsService.process(inductionUpdatedEvent)

    // Then
    verify(timelineService).recordTimelineEvent(eq(prisonNumber), capture(timelineEventCaptor))
    assertThat(timelineEventCaptor.value.sourceReference).isEqualTo(inductionUpdatedEvent.reference())
    assertThat(timelineEventCaptor.value.eventType).isEqualTo(TimelineEventType.INDUCTION_UPDATED)
    assertThat(timelineEventCaptor.value.prisonId).isEqualTo(inductionUpdatedEvent.prisonId())
    assertThat(timelineEventCaptor.value.actionedBy).isEqualTo(inductionUpdatedEvent.userId())
  }

  @Test
  fun `should fail to process unrecognised event`() {
    // Given
    val unrecognisedEvent = UnrecognisedInboundEvent()

    // When
    inboundEventsService.process(unrecognisedEvent)

    // Then
    assertThat(
      TestLogAppender.hasLog(
        "Unsupported event uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.UnrecognisedInboundEvent",
        Level.WARN,
      ),
    ).isTrue
  }
}

private class UnrecognisedInboundEvent : InboundEvent {
  override fun eventType() = "unknown-event-type"
  override fun reference() = UUID.randomUUID().toString()
  override fun prisonNumber() = aValidPrisonNumber()
  override fun occurredAt(): Instant = Instant.now()
}
