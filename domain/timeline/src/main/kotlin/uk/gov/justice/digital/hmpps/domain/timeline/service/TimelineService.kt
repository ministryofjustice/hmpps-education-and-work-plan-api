package uk.gov.justice.digital.hmpps.domain.timeline.service

import uk.gov.justice.digital.hmpps.domain.timeline.Timeline
import uk.gov.justice.digital.hmpps.domain.timeline.Timeline.Companion.newTimeline
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineNotFoundException
import java.time.LocalDate
import java.time.ZoneId

/**
 * Service class exposing methods that implement the business rules for the Timeline domain, and is how applications
 * must create and manage [Timeline]s.
 *
 * Applications using Timelines must new up an instance of this class providing an implementation of
 * [TimelinePersistenceAdapter].
 *
 * This class is deliberately final so that it cannot be subclassed, ensuring that the business rules stay within the
 * domain.
 */
class TimelineService(
  private val persistenceAdapter: TimelinePersistenceAdapter,
  private val prisonTimelineService: PrisonTimelineService,
) {

  /**
   * Records an [TimelineEvent] that has taken place for a prisoner.
   */
  fun recordTimelineEvent(prisonNumber: String, event: TimelineEvent) =
    persistenceAdapter.recordTimelineEvent(prisonNumber, event)

  /**
   * Records a collection of [TimelineEvent]s that have taken place for a prisoner.
   */
  fun recordTimelineEvents(prisonNumber: String, events: List<TimelineEvent>) =
    persistenceAdapter.recordTimelineEvents(prisonNumber, events)

  /**
   * Returns the [Timeline] for the prisoner identified by their prison number. Otherwise, throws
   * [TimelineNotFoundException] if it cannot be found.
   */
  fun getTimelineForPrisoner(
    prisonNumber: String,
    inductions: Boolean = false,
    goals: Boolean = false,
    reviews: Boolean = false,
    prisonEvents: Boolean = false,
    prisonId: String? = null,
    eventsSince: LocalDate? = null,
  ): Timeline {
    // Get the prisoner's main PLP timeline (for Induction/Goal related events)
    val prisonerTimeline = persistenceAdapter.getTimelineForPrisoner(prisonNumber)
    // Get their prison location history
    val prisonMovements = prisonTimelineService.getPrisonTimelineEvents(prisonNumber)

    // This should never happen as there should always be at least 1 prison movement (for when they entered prison)
    if (prisonerTimeline == null && prisonMovements.isEmpty()) {
      throw TimelineNotFoundException(prisonNumber)
    }

    val timeline = prisonerTimeline?.addEvents(prisonMovements) ?: newTimeline(prisonNumber, prisonMovements)

    // apply timeline filters
    applyFilter(
      timeline,
      inductions,
      goals,
      reviews,
      prisonEvents,
      prisonId,
      eventsSince,
    )

    return timeline
  }
}

private fun applyFilter(
  timeline: Timeline,
  inductions: Boolean,
  goals: Boolean,
  reviews: Boolean,
  prisonEvents: Boolean,
  prisonId: String? = null,
  eventsSince: LocalDate? = null,
) {
  // If no filtering criteria are applied, return all events
  if (!inductions && !goals && !reviews && !prisonEvents && prisonId == null && eventsSince == null) {
    return
  }

  // At least one of the filter options has been specified.
  //
  // The semantics of the filtering is such that if any of the booleans `inductions`, `goals`, `reviews` or `prisonEvents`
  // are set, then we only want the events of those types; else we want all events.
  // EG: If `inductions` only is true, then we only want Induction events, discarding all others
  //     If `prisonEvents` and `goals` are true, then we only want Prison Movement and Goal events, discarding all others
  //     If either no boolean option is true, or they are all true, then was want all events, discarding none
  //
  // Once we have the candidate set of events filtered by event type (as above), we should filter by prisonId and
  // eventsSince if set.

  val filteredTimeline: MutableSet<TimelineEvent> = mutableSetOf() // Use a set because some event types can be matched more than once

  // If none of the booleans are true the filtered list should contain all events
  if (!inductions && !goals && !reviews && !prisonEvents) {
    filteredTimeline.addAll(timeline.events)
  } else {
    if (inductions) {
      filteredTimeline.addAll(timeline.events.filter { it.eventType.isInduction })
    }
    if (goals) {
      filteredTimeline.addAll(timeline.events.filter { it.eventType.isGoal })
    }
    if (reviews) {
      filteredTimeline.addAll(timeline.events.filter { it.eventType.isReview })
    }
    if (prisonEvents) {
      filteredTimeline.addAll(timeline.events.filter { it.eventType.isPrisonEvent })
    }
  }

  // Now filter the candidate list by prisonId and eventsSince if set
  prisonId?.run {
    // remove any that don't match on prisonId
    filteredTimeline.removeIf { it.prisonId != this }
  }
  eventsSince?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.run {
    // remove any that are earlier than the specified date
    filteredTimeline.removeIf { it.timestamp.isBefore(this) }
  }

  timeline.events = filteredTimeline.toMutableList()
}
