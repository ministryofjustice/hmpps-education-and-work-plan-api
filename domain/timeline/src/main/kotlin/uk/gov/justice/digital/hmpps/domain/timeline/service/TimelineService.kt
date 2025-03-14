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
    inductions: Boolean? = null,
    goals: Boolean? = null,
    reviews: Boolean? = null,
    prisonEvents: Boolean? = null,
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
      inductions ?: false,
      goals ?: false,
      reviews ?: false,
      prisonEvents ?: false,
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

  val eventsSinceInstant = eventsSince?.atStartOfDay(ZoneId.systemDefault())?.toInstant()

  timeline.events.removeIf { event ->

    // if any of the filters return a true match then the event won't be removed from the list.
    val anyEventTypeMatches =
      inductions && event.eventType.isInduction ||
        goals && event.eventType.isGoal ||
        reviews && event.eventType.isReview ||
        prisonEvents && event.eventType.isPrisonEvent

    val prisonMatches = prisonId == null || event.prisonId == prisonId

    val eventSinceMatches = eventsSinceInstant == null || event.timestamp.isAfter(eventsSinceInstant)

    // Remove event where there are no matches
    !(anyEventTypeMatches && prisonMatches && eventSinceMatches)
  }
}
