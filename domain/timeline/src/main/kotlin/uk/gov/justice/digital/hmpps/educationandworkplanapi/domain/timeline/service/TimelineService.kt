package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.Timeline

/**
 * Interface defining methods for how applications must create and manage [Timeline]s.
 */
interface TimelineService {

  /**
   * Returns the [Timeline] for the prisoner identified by their prison number.
   */
  fun getTimelineForPrisoner(prisonNumber: String): Timeline
}
