package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.Timeline

/**
 * Default implementation of [TimelineService].
 *
 * Applications using Timelines must new up an instance of this class providing an implementation of
 * [TimelinePersistenceAdapter].
 *
 * This class is deliberately final so that it cannot be subclassed, ensuring that the business rules stay within the
 * domain.
 */
class DefaultTimelineService(
  private val persistenceAdapter: TimelinePersistenceAdapter,
) : TimelineService {

  /**
   * Returns the [Timeline] for the prisoner identified by their prison number.
   */
  override fun getTimelineForPrisoner(prisonNumber: String): Timeline {
    val timelineEvents = persistenceAdapter.getTimelineEventsForPrisoner(prisonNumber)
    return Timeline(prisonNumber, timelineEvents)
  }
}
