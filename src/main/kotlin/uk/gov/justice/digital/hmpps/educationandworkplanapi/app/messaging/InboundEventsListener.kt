package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.awspring.cloud.sqs.annotation.SqsListener
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}
private const val NOTIFICATION = "Notification"

@Component
class InboundEventsListener(
  private val mapper: ObjectMapper,
  private val inboundEventsService: InboundEventsService,
) {

  @SqsListener("educationandworkplan", factory = "hmppsQueueContainerFactoryProxy")
  internal fun onMessage(sqsMessage: SqsMessage) {
    log.debug { "Inbound event message: $sqsMessage" }

    when (sqsMessage.Type) {
      NOTIFICATION -> {
        mapper.readValue<InboundEvent>(sqsMessage.Message).let {
          log.info { "Processing inbound event ${it.eventType}" }
          inboundEventsService.process(it)
        }
      }

      else -> log.info { "Unrecognised message type: ${sqsMessage.Type}" }
    }
  }
}
