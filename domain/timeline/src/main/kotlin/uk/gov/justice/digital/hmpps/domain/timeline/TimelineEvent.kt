package uk.gov.justice.digital.hmpps.domain.timeline

import java.time.Instant
import java.util.UUID

/**
 * A TimelineEvent represents a single event that occurred whilst a prisoner has been in prison.
 *
 * Once created a TimelineEvent cannot be mutated in any way. It represents a single event at a point in time.
 */
data class TimelineEvent(
  /**
   * A unique reference for each event.
   */
  val reference: UUID,
  /**
   * The reference of the entity that's being updated, such as a Goal or an individual Goal's Step. A [String] rather
   * than a [UUID], because we cannot guarantee the format from other systems (e.g. the ciag-induction-service).
   */
  val sourceReference: String,
  /**
   * The type of the event - see [TimelineEventType].
   */
  val eventType: TimelineEventType,
  /**
   * A map of useful contextual information about the event.
   */
  val contextualInfo: Map<TimelineEventContext, String>?,
  /**
   * The ID of the Prison where the Prisoner is based at the time of the event.
   */
  val prisonId: String,
  /**
   * The username of the person who caused this event. Set to 'system' if the event was not actioned by a DPS user.
   */
  val actionedBy: String,
  /**
   * The name of the person who caused this event (if applicable).
   */
  val actionedByDisplayName: String? = null,
  /**
   * The date and time when the event occurred.
   */
  val timestamp: Instant,
  /**
   * A correlationId for this and any other TimelineEvents that occurred at the same time (i.e. within same
   * atomic action). For example, this could be an update to a Goal and one or more of its child Steps.
   */
  val correlationId: UUID,
) {
  companion object {
    fun newTimelineEvent(
      sourceReference: String,
      eventType: TimelineEventType,
      contextualInfo: Map<TimelineEventContext, String>? = null,
      prisonId: String,
      actionedBy: String,
      actionedByDisplayName: String? = null,
      timestamp: Instant = Instant.now(),
      correlationId: UUID = UUID.randomUUID(),
    ) = TimelineEvent(
      reference = UUID.randomUUID(),
      sourceReference = sourceReference,
      eventType = eventType,
      contextualInfo = contextualInfo,
      prisonId = prisonId,
      actionedBy = actionedBy,
      actionedByDisplayName = actionedByDisplayName,
      timestamp = timestamp,
      correlationId = correlationId,
    )
  }
}

/**
 * The events that the business are interested in (for example to display a history of these events on screen).
 */
enum class TimelineEventType {
  // Induction events
  INDUCTION_CREATED,
  INDUCTION_UPDATED,
  INDUCTION_SCHEDULE_CREATED,
  INDUCTION_SCHEDULE_UPDATED,

  // Action Plan/Goal/Step events
  ACTION_PLAN_CREATED,
  GOAL_CREATED,
  GOAL_UPDATED,
  GOAL_COMPLETED,
  GOAL_ARCHIVED,
  GOAL_UNARCHIVED,
  STEP_UPDATED,
  STEP_NOT_STARTED,
  STEP_STARTED,
  STEP_COMPLETED,

  // Action Plan Review events
  ACTION_PLAN_REVIEW_COMPLETED,

  // Conversation/Notes events
  CONVERSATION_CREATED,
  CONVERSATION_UPDATED,

  // Prison movement events
  PRISON_ADMISSION,
  PRISON_RELEASE,
  PRISON_TRANSFER,
}

enum class TimelineEventContext {
  GOAL_TITLE,
  STEP_TITLE,
  GOAL_ARCHIVED_REASON,
  GOAL_ARCHIVED_REASON_OTHER,
  CONVERSATION_TYPE,
  PRISON_TRANSFERRED_FROM,
  INDUCTION_SCHEDULE_STATUS,
  INDUCTION_SCHEDULE_DEADLINE_DATE,
  COMPLETED_REVIEW_ENTERED_ONLINE_AT,
  COMPLETED_REVIEW_ENTERED_ONLINE_BY,
  COMPLETED_REVIEW_CONDUCTED_IN_PERSON_DATE,
  COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY,
  COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY_ROLE,
}
