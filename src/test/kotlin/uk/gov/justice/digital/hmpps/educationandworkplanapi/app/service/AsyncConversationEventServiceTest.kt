package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

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
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.aValidConversation
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService

@ExtendWith(MockitoExtension::class)
class AsyncConversationEventServiceTest {

  companion object {
    private val IGNORED_FIELDS = arrayOf("reference", "correlationId")
  }

  @InjectMocks
  private lateinit var conversationEventService: AsyncConversationEventService

  @Mock
  private lateinit var timelineService: TimelineService

  @Mock
  private lateinit var telemetryService: TelemetryService

  @Captor
  private lateinit var timelineEventCaptor: ArgumentCaptor<TimelineEvent>

  @Test
  fun `should handle conversation created`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val conversation = aValidConversation(prisonNumber = prisonNumber)
    val expectedTimelineEvent =
      with(conversation) {
        TimelineEvent.newTimelineEvent(
          sourceReference = reference.toString(),
          eventType = TimelineEventType.CONVERSATION_CREATED,
          prisonId = note.createdAtPrison,
          actionedBy = note.createdBy!!,
          timestamp = note.createdAt!!,
          contextualInfo = mapOf(TimelineEventContext.CONVERSATION_TYPE to "REVIEW"),
        )
      }

    // When
    conversationEventService.conversationCreated(conversation)

    // Then
    verify(timelineService).recordTimelineEvent(eq(prisonNumber), capture(timelineEventCaptor))
    verify(telemetryService).trackConversationCreated(conversation)
    assertThat(timelineEventCaptor.value) //
      .usingRecursiveComparison()
      .ignoringFields(*IGNORED_FIELDS)
      .isEqualTo(expectedTimelineEvent)
  }

  @Test
  fun `should handle conversation updated`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val conversation = aValidConversation(prisonNumber = prisonNumber)
    val expectedTimelineEvent =
      with(conversation) {
        TimelineEvent.newTimelineEvent(
          sourceReference = reference.toString(),
          eventType = TimelineEventType.CONVERSATION_UPDATED,
          prisonId = note.lastUpdatedAtPrison,
          actionedBy = note.lastUpdatedBy!!,
          timestamp = note.lastUpdatedAt!!,
          contextualInfo = mapOf(TimelineEventContext.CONVERSATION_TYPE to "REVIEW"),
        )
      }

    // When
    conversationEventService.conversationUpdated(conversation)

    // Then
    verify(timelineService).recordTimelineEvent(eq(prisonNumber), capture(timelineEventCaptor))
    verify(telemetryService).trackConversationUpdated(conversation)
    assertThat(timelineEventCaptor.value) //
      .usingRecursiveComparison()
      .ignoringFields(*IGNORED_FIELDS)
      .isEqualTo(expectedTimelineEvent)
  }
}
