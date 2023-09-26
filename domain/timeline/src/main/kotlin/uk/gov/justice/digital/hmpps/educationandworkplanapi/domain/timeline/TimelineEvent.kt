package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline

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
   * Some useful contextual information about the event. For example a reason for the event.
   */
  val contextualInfo: String?,
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
) {
  companion object {
    fun newTimelineEvent(
      sourceReference: String,
      eventType: TimelineEventType,
      contextualInfo: String? = null,
      prisonId: String,
      actionedBy: String,
      actionedByDisplayName: String? = null,
      timestamp: Instant = Instant.now(),
    ) = TimelineEvent(
      reference = UUID.randomUUID(),
      sourceReference = sourceReference,
      eventType = eventType,
      contextualInfo = contextualInfo,
      prisonId = prisonId,
      actionedBy = actionedBy,
      actionedByDisplayName = actionedByDisplayName,
      timestamp = timestamp,
    )
  }
}

/**
 * The events that the business are interested in (for example to display a history of these events on screen).
 *
 * These are currently limited to CIAG induction and PLP related events, but could be expanded in the future.
 */
enum class TimelineEventType {
  INDUCTION_CREATED,
  INDUCTION_UPDATED,
  ACTION_PLAN_CREATED,
  GOAL_CREATED,
  GOAL_UPDATED,
  GOAL_STARTED,
  GOAL_COMPLETED,
  GOAL_ARCHIVED,
  STEP_UPDATED,
  STEP_NOT_STARTED,
  STEP_STARTED,
  STEP_COMPLETED,
}
