package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
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
  private val inductionScheduleService: InductionScheduleService,
) {

  @Transactional
  fun process(assessmentEvent: AssessmentEventDto) {
    val prisonNumber = assessmentEvent.prisonNumber
    log.info { "Processing assessment event for prisoner [$prisonNumber]" }

    val prisoner = runCatching { prisonerSearchApiService.getPrisoner(prisonNumber) }.getOrNull()
    val prisonId = prisoner?.prisonId ?: "N/A"

    val entity = educationAssessmentEventEntityMapper.fromDtoToEntity(assessmentEvent, prisonId)
    educationAssessmentEventRepository.saveAndFlush(entity)

    // Under the PES contract, completing the prisoner's S&As schedules their Induction if it was awaiting them.
    // This is a no-op under PEF (no Induction Schedule is ever in the pending-S&As state).
    inductionScheduleService.schedulePendingInductionSchedule(prisonNumber, prisonId)

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
