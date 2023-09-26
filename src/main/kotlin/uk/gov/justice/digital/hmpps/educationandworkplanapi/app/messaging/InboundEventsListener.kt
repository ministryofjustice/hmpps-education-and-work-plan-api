package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.module.kotlin.readValue
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class InboundEventsListener(
  private val mapper: ObjectMapper,
  private val inboundEventsService: InboundEventsService,
) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  init {
    log.info("Event listener started.")
  }

  @SqsListener("education-and-work-plan", factory = "hmppsQueueContainerFactoryProxy")
  internal fun onMessage(rawMessage: String) {
    log.info("Inbound event raw message: $rawMessage")

    val sqsMessage: SQSMessage = mapper.readValue(rawMessage)

    when (sqsMessage.Type) {
      "Notification" -> {
        mapper.readValue<HMPPSDomainEvent>(sqsMessage.Message).let { domainEvent ->
          domainEvent.toInboundEventType()?.let { inboundEventType ->
            log.info("Processing inbound event $inboundEventType")
            inboundEventsService.process(inboundEventType.toInboundEvent(mapper, sqsMessage.Message))
          } ?: log.info("Ignoring domain event ${domainEvent.eventType}")
        }
      }

      else -> log.info("Unrecognised message type: ${sqsMessage.Type}")
    }
  }
}

data class HMPPSDomainEvent(
  val eventType: String,
) {
  fun toInboundEventType() = InboundEventType.values().firstOrNull { it.eventType == eventType }
}

@JsonNaming(value = PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class SQSMessage(val Type: String, val Message: String, val MessageId: String? = null)
