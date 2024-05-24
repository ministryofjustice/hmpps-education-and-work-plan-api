package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.Conversation
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.service.ConversationEventService
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService

private val log = KotlinLogging.logger {}

/**
 * Implementation of [ConversationEventService] for performing additional asynchronous actions related to [Conversation]
 * events.
 */
@Component
@Async
class AsyncConversationEventService(
  private val timelineService: TimelineService,
  private val telemetryService: TelemetryService,
) : ConversationEventService {
  override fun conversationCreated(createdConversation: Conversation) {
    log.debug { "Conversation created event for prisoner [${createdConversation.prisonNumber}]" }
    timelineService.recordTimelineEvent(createdConversation.prisonNumber, buildConversationCreatedEvent(createdConversation))
    telemetryService.trackConversationCreated(conversation = createdConversation)
  }

  override fun conversationUpdated(updatedConversation: Conversation) {
    log.debug { "Conversation updated event for prisoner [${updatedConversation.prisonNumber}]" }
    timelineService.recordTimelineEvent(updatedConversation.prisonNumber, buildConversationUpdatedEvent(updatedConversation))
    telemetryService.trackConversationUpdated(conversation = updatedConversation)
  }

  private fun buildConversationCreatedEvent(conversation: Conversation): TimelineEvent =
    with(conversation) {
      TimelineEvent.newTimelineEvent(
        sourceReference = reference.toString(),
        eventType = TimelineEventType.CONVERSATION_CREATED,
        contextualInfo = buildContextualInfo(this),
        prisonId = note.createdAtPrison,
        actionedBy = note.createdBy!!,
        actionedByDisplayName = note.createdByDisplayName,
        timestamp = note.createdAt!!,
      )
    }

  private fun buildConversationUpdatedEvent(conversation: Conversation): TimelineEvent =
    with(conversation) {
      TimelineEvent.newTimelineEvent(
        sourceReference = reference.toString(),
        eventType = TimelineEventType.CONVERSATION_UPDATED,
        contextualInfo = buildContextualInfo(this),
        prisonId = note.lastUpdatedAtPrison,
        actionedBy = note.lastUpdatedBy!!,
        actionedByDisplayName = note.lastUpdatedByDisplayName,
        timestamp = note.lastUpdatedAt!!,
      )
    }

  /**
   * Generates and returns the contextual information for a Conversation Timeline Event.
   */
  private fun buildContextualInfo(conversation: Conversation): Map<TimelineEventContext, String> =
    mapOf(TimelineEventContext.CONVERSATION_TYPE to conversation.type.toString())
}
