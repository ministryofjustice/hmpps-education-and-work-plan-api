package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import io.awspring.cloud.sqs.annotation.SqsListener
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
@ConditionalOnProperty(name = ["hmpps.sqs.enabled"], havingValue = "true")
class InboundAssessmentEventsListener {

  @SqsListener("assessmentevents", factory = "hmppsQueueContainerFactoryProxy")
  internal fun onMessage(sqsAssessmentEventMessage: SqsAssessmentEventMessage) {
    log.debug { "Inbound sqs assessment event message: $sqsAssessmentEventMessage" }
  }
}
