package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import io.awspring.cloud.sqs.annotation.SqsListener
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.educationassessment.EducationAssessmentEventEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.EducationAssessmentEventRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.TelemetryService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.TimelineEventFactory

private val log = KotlinLogging.logger {}

@Component
@ConditionalOnProperty(name = ["hmpps.sqs.enabled"], havingValue = "true")
class InboundAssessmentEventsListener(
  private val educationAssessmentEventRepository: EducationAssessmentEventRepository,
  private val educationAssessmentEventEntityMapper: EducationAssessmentEventEntityMapper,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val timelineService: TimelineService,
  private val timelineEventFactory: TimelineEventFactory,
  private val telemetryService: TelemetryService,
) {

  @SqsListener("assessmentevents", factory = "hmppsQueueContainerFactoryProxy")
  internal fun onMessage(sqsAssessmentEventMessage: SqsAssessmentEventMessage) {
    val prisonNumber = sqsAssessmentEventMessage.messageAttributes.prisonNumber
    log.info { "Processing assessment event for prisoner [$prisonNumber]" }

    val prisoner = prisonerSearchApiService.getPrisoner(prisonNumber)
    val prisonId = prisoner.prisonId ?: "N/A"

    val entity = educationAssessmentEventEntityMapper.fromMessageToEntity(sqsAssessmentEventMessage, prisonId)
    educationAssessmentEventRepository.saveAndFlush(entity)

    val timelineEvent = timelineEventFactory.educationAssessmentEventCreatedEvent(entity.reference.toString(), prisonId)
    timelineService.recordTimelineEvent(prisonNumber, timelineEvent)

    telemetryService.trackEducationAssessmentEventCreated(entity)

    log.info { "Saved education assessment event [${entity.reference}] for prisoner [$prisonNumber]" }
  }
}
