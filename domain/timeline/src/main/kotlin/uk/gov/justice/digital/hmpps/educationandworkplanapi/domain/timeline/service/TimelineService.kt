package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.Timeline
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent

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
) {

  /**
   * Records an [TimelineEvent] that has taken place for a prisoner.
   */
  fun recordTimelineEvent(prisonNumber: String, event: TimelineEvent) {
    persistenceAdapter.recordTimelineEvent(prisonNumber, event)
  }

  /**
   * Records a collection of [TimelineEvent]s that have taken place for a prisoner.
   */
  fun recordTimelineEvents(prisonNumber: String, events: List<TimelineEvent>) {
    persistenceAdapter.recordTimelineEvents(prisonNumber, events)
  }

  /**
   * Returns the [Timeline] for the prisoner identified by their prison number.
   */
  fun getTimelineForPrisoner(prisonNumber: String): Timeline {
    val timelineEvents = persistenceAdapter.getTimelineEventsForPrisoner(prisonNumber)
    return Timeline(prisonNumber, timelineEvents)
  }
}
