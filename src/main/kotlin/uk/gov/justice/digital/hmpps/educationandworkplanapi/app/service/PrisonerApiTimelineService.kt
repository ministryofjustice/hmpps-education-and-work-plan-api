package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.PrisonApiClient
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.PrisonApiException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.mapper.PrisonMovementEventsMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service.PrisonTimelineService

private val log = KotlinLogging.logger {}

@Component
class PrisonerApiTimelineService(
  private val prisonApiClient: PrisonApiClient,
  private val prisonMovementEventsMapper: PrisonMovementEventsMapper,
) : PrisonTimelineService {

  override fun getPrisonTimelineEvents(prisonNumber: String): List<TimelineEvent> {
    return try {
      val prisonMovementEvents = prisonApiClient.getPrisonMovementEvents(prisonNumber)
      log.info { "Retrieved prison movements for prisoner $prisonNumber" }
      return prisonMovementEventsMapper.toTimelineEvents(prisonMovementEvents)
    } catch (e: PrisonApiException) {
      log.error("Error retrieving prison movements for prisoner $prisonNumber", e)
      emptyList()
    }
  }
}
