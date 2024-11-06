package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import uk.gov.justice.hmpps.sqs.publish
import java.net.URI
import java.time.Instant

@Component
class InductionScheduleUpdateEventPusher(
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
  @Value("\${service.base-url}") private val serviceBaseUrl: String,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)

    const val EVENT_TYPE = "plp.induction-schedule.updated"
    const val CONTENT_TYPE = "text/plain;charset=UTF-8"
  }

  internal val eventTopic by lazy { hmppsQueueService.findByTopicId("domainevents") as HmppsTopic }

  fun sendEvent(prisonerNumber: String, occurredAt: Instant) {
    val cne = HmppsDomainEvent(prisonerNumber, serviceBaseUrl, occurredAt)
    log.info("Pushing induction schedule updated event to event topic person reference $prisonerNumber")
    val publishResponse = eventTopic.publish(
      cne.eventType,
      objectMapper.writeValueAsString(cne),
      attributes = mapOf(
        "eventType" to MessageAttributeValue.builder().dataType("String").stringValue(cne.eventType).build(),
        "contentType" to MessageAttributeValue.builder().dataType("String")
          .stringValue(CONTENT_TYPE).build(),
      ),
    )
    log.debug("Sent case induction schedule updated with message id {}", publishResponse.messageId())
  }

  data class HmppsDomainEvent(
    val version: Int = 1,
    val eventType: String = EVENT_TYPE,
    val description: String = "A prisoner learning plan induction schedule created or amended",
    val detailUrl: String,
    val occurredAt: Instant,
    val personReference: PersonReference,
  ) {
    constructor(prisonerNumber: String, baseUrl: String, occurredAt: Instant) : this(
      detailUrl = URI.create("$baseUrl/inductions/$prisonerNumber/induction-schedule").toString(),
      personReference = PersonReference(identifiers = listOf(Identifier("NOMS", prisonerNumber))),
      occurredAt = occurredAt,
    )
  }
}
