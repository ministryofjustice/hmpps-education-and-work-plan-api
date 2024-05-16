package uk.gov.justice.digital.hmpps.domain.timeline.service

import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent

interface PrisonTimelineService {

  /**
   * Returns a [List] of [TimelineEvent]s that represent whenever a Prisoner entered or left a Prison, or transferred
   * between Prisons.
   */
  fun getPrisonTimelineEvents(prisonNumber: String): List<TimelineEvent>
}
