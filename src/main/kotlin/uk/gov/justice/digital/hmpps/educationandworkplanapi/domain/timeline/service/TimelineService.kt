package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.Timeline

/**
 * Service class exposing methods that implement the business rules for the Timeline domain, and is how applications
 * must create and manage [Timeline]s.
 *
 * Applications using Timelines must new up an instance of this class providing an implementation of
 * [TimelinePersistenceService].
 *
 * This class is deliberately final so that it cannot be subclassed, ensuring that the business rules stay within the
 * domain.
 */
class TimelineService(
  private val persistenceService: TimelinePersistenceService,
) {

  /**
   * Returns the [Timeline] for the prisoner identified by their prison number.
   */
  fun getTimelineForPrisoner(prisonNumber: String): Timeline {
    val timelineEvents = persistenceService.getTimelineEventsForPrisoner(prisonNumber)
    return Timeline(prisonNumber, timelineEvents)
  }
}
