package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.Timeline
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent

/**
 * Persistence Adapter for [Timeline] instances
 *
 * Implementations should use the underlying persistence of the application in question, eg: JPA, Mongo, Dynamo, Redis etc
 *
 * Implementations should not throw exceptions. These are not part of the interface and are not checked or handled by
 * [TimelineService].
 */
interface TimelinePersistenceAdapter {

  /**
   * Find and return the [TimelineEvent]s for the prisoner identified by their prison number.
   * Returns an empty collection if there are no TimelineEvents for the specified prisoner.
   */
  fun getTimelineEventsForPrisoner(prisonNumber: String): List<TimelineEvent>
}
