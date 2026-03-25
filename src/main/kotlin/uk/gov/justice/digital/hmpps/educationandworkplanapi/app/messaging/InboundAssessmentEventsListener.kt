package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import io.awspring.cloud.sqs.annotation.SqsListener
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.AssessmentEventDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.AssessmentEventStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.EducationAssessmentEventService

@Component
@ConditionalOnProperty(name = ["hmpps.sqs.enabled"], havingValue = "true")
class InboundAssessmentEventsListener(
  private val educationAssessmentEventService: EducationAssessmentEventService,
) {

  @SqsListener("assessmentevents", factory = "hmppsQueueContainerFactoryProxy")
  internal fun onMessage(sqsAssessmentEventMessage: SqsAssessmentEventMessage) {
    educationAssessmentEventService.process(sqsAssessmentEventMessage.toDto())
  }
}

private fun SqsAssessmentEventMessage.toDto(): AssessmentEventDto = with(messageAttributes) {
  AssessmentEventDto(
    prisonNumber = prisonNumber,
    status = status.toServiceStatus(),
    statusChangeDate = statusChangeDate,
    detailUrl = detailUrl,
  )
}

private fun EducationAssessmentStatus.toServiceStatus(): AssessmentEventStatus = when (this) {
  EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE -> AssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE
}
