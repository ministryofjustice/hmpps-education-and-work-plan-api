package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.Timeline
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent

/**
 * Persistence Adapter for [Timeline] instances.
 *
 * Implementations should use the underlying persistence of the application in question, eg: JPA, Mongo, Dynamo,
 * Redis etc.
 *
 * Implementations should not throw exceptions. These are not part of the interface and are not checked or handled by.
 * [TimelineService].
 */
interface TimelinePersistenceAdapter {

  /**
   * Records an [TimelineEvent] that has taken place for a prisoner. Creates a new Timeline for the prisoner if it does
   * not already exist.
   *
   * @return The [TimelineEvent] with any newly generated values (if applicable).
   */
  fun recordTimelineEvent(prisonNumber: String, event: TimelineEvent): TimelineEvent

  /**
   * Records multiple [TimelineEvent]s that have taken place for a prisoner. Creates a new Timeline for the prisoner if
   * it does not already exist.
   *
   * @return The updated [Timeline] containing all its events.
   */
  fun recordTimelineEvents(prisonNumber: String, events: List<TimelineEvent>): Timeline

  /**
   * Finds and returns the [Timeline] for the prisoner identified by their prison number.
   * Returns null if the prisoner does not have a [Timeline] (i.e. no [TimelineEvent]s recorded).
   */
  fun getTimelineForPrisoner(prisonNumber: String): Timeline?
}
