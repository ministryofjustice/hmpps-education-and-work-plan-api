package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.educationassessment.EducationAssessmentEventEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.EducationAssessmentEventRepository

private val log = KotlinLogging.logger {}

@Service
class EducationAssessmentEventService(
  private val educationAssessmentEventRepository: EducationAssessmentEventRepository,
  private val educationAssessmentEventEntityMapper: EducationAssessmentEventEntityMapper,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val timelineService: TimelineService,
  private val timelineEventFactory: TimelineEventFactory,
  private val telemetryService: TelemetryService,
) {

  fun process(assessmentEvent: AssessmentEventDto) {
    val prisonNumber = assessmentEvent.prisonNumber
    log.info { "Processing assessment event for prisoner [$prisonNumber]" }

    val prisoner = prisonerSearchApiService.getPrisoner(prisonNumber)
    val prisonId = prisoner.prisonId ?: "N/A"

    val entity = educationAssessmentEventEntityMapper.fromDtoToEntity(assessmentEvent, prisonId)
    educationAssessmentEventRepository.saveAndFlush(entity)

    val timelineEvent = timelineEventFactory.educationAssessmentEventCreatedEvent(entity.reference.toString(), prisonId)
    timelineService.recordTimelineEvent(prisonNumber, timelineEvent)

    telemetryService.trackEducationAssessmentEventCreated(entity)

    log.info { "Saved education assessment event [${entity.reference}] for prisoner [$prisonNumber]" }
  }
}
