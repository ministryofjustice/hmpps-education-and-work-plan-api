package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.PrisonApiClient
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service.PrisonTimelineService

@Component
class PrisonerApiTimelineService(private val prisonApiClient: PrisonApiClient) : PrisonTimelineService {

  override fun getPrisonTimelineEvents(prisonNumber: String): List<TimelineEvent> {
    // TODO RR-566 map to a list of TimelineEvents
    // val prisonMovements = prisonApiClient.getPrisonMovementEvents(prisonNumber)
    return emptyList()
  }
}
