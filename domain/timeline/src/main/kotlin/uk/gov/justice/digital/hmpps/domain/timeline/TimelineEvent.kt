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
  val contextualInfo: Map<TimelineEventContext, String>,
  /**
   * The ID of the Prison where the Prisoner is based at the time of the event.
   */
  val prisonId: String,
  /**
   * The username of the person who caused this event. Set to 'system' if the event was not actioned by a DPS user.
   */
  val actionedBy: String,
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
      contextualInfo: Map<TimelineEventContext, String> = emptyMap(),
      prisonId: String,
      actionedBy: String,
      timestamp: Instant = Instant.now(),
      correlationId: UUID = UUID.randomUUID(),
    ) = TimelineEvent(
      reference = UUID.randomUUID(),
      sourceReference = sourceReference,
      eventType = eventType,
      contextualInfo = contextualInfo,
      prisonId = prisonId,
      actionedBy = actionedBy,
      timestamp = timestamp,
      correlationId = correlationId,
    )
  }
}

/**
 * The events that the business are interested in (for example to display a history of these events on screen).
 */
enum class TimelineEventType(val isReview: Boolean, val isGoal: Boolean, val isInduction: Boolean) {
  // Induction events
  INDUCTION_CREATED(false, false, true),
  INDUCTION_UPDATED(false, false, true),
  INDUCTION_SCHEDULE_CREATED(false, false, true),
  INDUCTION_SCHEDULE_UPDATED(false, false, true),
  INDUCTION_SCHEDULE_STATUS_UPDATED(false, false, true),

  // Action Plan/Goal/Step events
  ACTION_PLAN_CREATED(false, true, false),
  GOAL_CREATED(false, true, false),
  GOAL_UPDATED(false, true, false),
  GOAL_COMPLETED(false, true, false),
  GOAL_ARCHIVED(false, true, false),
  GOAL_UNARCHIVED(false, true, false),
  STEP_UPDATED(false, true, false),
  STEP_NOT_STARTED(false, true, false),
  STEP_STARTED(false, true, false),
  STEP_COMPLETED(false, true, false),

  // Action Plan Review events
  ACTION_PLAN_REVIEW_COMPLETED(true, false, false),
  ACTION_PLAN_REVIEW_SCHEDULE_STATUS_UPDATED(true, false, false),
  ACTION_PLAN_REVIEW_SCHEDULE_CREATED(true, false, false),

  // Prison movement events (not related to induction, goals, or reviews)
  PRISON_ADMISSION(false, false, false),
  PRISON_RELEASE(false, false, false),
  PRISON_TRANSFER(false, false, false),
}

enum class TimelineEventContext {
  GOAL_TITLE,
  STEP_TITLE,
  GOAL_ARCHIVED_REASON,
  GOAL_ARCHIVED_REASON_OTHER,
  PRISON_TRANSFERRED_FROM,
  INDUCTION_SCHEDULE_STATUS,
  INDUCTION_SCHEDULE_DEADLINE_DATE,
  COMPLETED_INDUCTION_ENTERED_ONLINE_AT,
  COMPLETED_INDUCTION_ENTERED_ONLINE_BY,
  COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_DATE,
  COMPLETED_INDUCTION_NOTES,
  COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_BY,
  COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_BY_ROLE,
  COMPLETED_REVIEW_ENTERED_ONLINE_AT,
  COMPLETED_REVIEW_ENTERED_ONLINE_BY,
  COMPLETED_REVIEW_NOTES,
  COMPLETED_REVIEW_CONDUCTED_IN_PERSON_DATE,
  COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY,
  COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY_ROLE,
  REVIEW_SCHEDULE_STATUS_OLD,
  REVIEW_SCHEDULE_STATUS_NEW,
  REVIEW_SCHEDULE_DEADLINE_OLD,
  REVIEW_SCHEDULE_DEADLINE_NEW,
  REVIEW_SCHEDULE_EXEMPTION_REASON,
  INDUCTION_SCHEDULE_STATUS_OLD,
  INDUCTION_SCHEDULE_STATUS_NEW,
  INDUCTION_SCHEDULE_DEADLINE_OLD,
  INDUCTION_SCHEDULE_DEADLINE_NEW,
  INDUCTION_SCHEDULE_EXEMPTION_REASON,
}
