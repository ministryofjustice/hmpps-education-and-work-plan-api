package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.personallearningplan.EmployabilitySkill
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.EmployabilitySkillsEventService
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService

private val log = KotlinLogging.logger {}

/**
 * Implementation of [AsyncEmployabilitySkillsEventService] for performing additional asynchronous actions related to [EmployabilitySkill]
 * events.
 */
@Component
@Async
class AsyncEmployabilitySkillsEventService(
  private val telemetryService: TelemetryService,
  private val timelineEventFactory: TimelineEventFactory,
  private val timelineService: TimelineService,
) : EmployabilitySkillsEventService {
  override fun employabilitySkillsCreated(employabilitySkills: List<EmployabilitySkill>) {
    log.info { "Employability skills created event for ${employabilitySkills.size} prisoners" }

    if (employabilitySkills.isEmpty()) return

    employabilitySkills.forEach {
      log.info {
        "Employability skill created event for prisoner [${it.prisonNumber}]"
        telemetryService.trackEmployabilitySkillCreated(it)
      }
    }

    val prisonNumber = employabilitySkills.first().prisonNumber

    val timelineEvents = timelineEventFactory.employabilitySkillsCreatedEvent(employabilitySkills)
    timelineService.recordTimelineEvents(prisonNumber, timelineEvents)
  }
}
