package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service.TimelinePersistenceAdapter

private val log = KotlinLogging.logger {}

@Component
class JpaTimelinePersistenceAdapter(
  // private val timelineRepository: TimelineRepository,
  // private val timelineMapper: TimelineEntityMapper,
) : TimelinePersistenceAdapter {

  @Transactional
  override fun recordTimelineEvent(prisonNumber: String, event: TimelineEvent) {
    log.info { "Recording TimelineEvent [$event] for prisoner [$prisonNumber]" }
    // TODO RR-317
  }

  @Transactional
  override fun recordTimelineEvents(prisonNumber: String, events: List<TimelineEvent>) {
    log.info { "Recording [${events.size}] TimelineEvents for prisoner [$prisonNumber]" }
    // TODO RR-317
  }

  @Transactional
  override fun getTimelineEventsForPrisoner(prisonNumber: String): List<TimelineEvent> {
    log.info { "Getting TimelineEvents for prisoner [$prisonNumber]" }
    return emptyList()
    // TODO RR-317
  }
}
