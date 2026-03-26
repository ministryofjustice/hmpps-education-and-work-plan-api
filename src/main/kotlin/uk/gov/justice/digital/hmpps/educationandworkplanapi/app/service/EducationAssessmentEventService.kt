package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventEntity
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

  @Transactional
  fun process(assessmentEvent: AssessmentEventDto) {
    val prisonNumber = assessmentEvent.prisonNumber
    log.info { "Processing assessment event for prisoner [$prisonNumber]" }

    val prisoner = prisonerSearchApiService.getPrisoner(prisonNumber)
    val prisonId = prisoner.prisonId ?: "N/A"

    val entity = educationAssessmentEventEntityMapper.fromDtoToEntity(assessmentEvent, prisonId)
    educationAssessmentEventRepository.saveAndFlush(entity)

    performFollowOnEvents(entity)

    log.info { "Saved education assessment event [${entity.reference}] for prisoner [$prisonNumber]" }
  }

  @Async
  fun performFollowOnEvents(entity: EducationAssessmentEventEntity) = with(entity) {
    timelineEventFactory.educationAssessmentEventCreatedEvent(reference.toString(), createdAtPrison).also {
      timelineService.recordTimelineEvent(prisonNumber, it)
    }

    telemetryService.trackEducationAssessmentEventCreated(this)
  }
}
