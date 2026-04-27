package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.EmployabilitySkill
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.service.EmployabilitySkillsEventService
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService
import java.util.UUID

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
    if (employabilitySkills.isNotEmpty()) {
      val correlationId = UUID.randomUUID()
      val prisonNumber = employabilitySkills.first().prisonNumber

      log.debug {
        "Employability skills created event for prisoner [$prisonNumber]"
      }

      timelineService.recordTimelineEvents(
        prisonNumber,
        timelineEventFactory.employabilitySkillsCreatedTimelineEvents(employabilitySkills, correlationId),
      )

      employabilitySkills.forEach {
        telemetryService.trackEmployabilitySkillCreated(it)
      }
    }
  }
}
