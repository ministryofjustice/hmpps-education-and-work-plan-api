package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import uk.gov.justice.hmpps.sqs.eventTypeMessageAttributes
import java.net.URI
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

private val log = KotlinLogging.logger {}
const val EVENT_TYPE = "plp.induction-schedule.updated"

@Component
class InductionScheduleUpdateEventPublisher(
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
  @Value("\${service.base-url}") private val serviceBaseUrl: String,
) {

  internal val eventTopic by lazy { hmppsQueueService.findByTopicId("domainevents") as HmppsTopic }

  fun sendEvent(prisonerNumber: String, occurredAt: Instant) {
    val event = HmppsDomainEvent(
      prisonerNumber,
      serviceBaseUrl,
      occurredAt
        .atZone(ZoneId.of("Europe/London")).toLocalDateTime(),
    )
    log.info("Pushing induction schedule updated event to event topic person reference $prisonerNumber")
    publishToOutboundTopic(event)
    log.debug("Sent induction schedule updated event")
  }

  fun publishToOutboundTopic(hmppsEvent: HmppsDomainEvent) {
    eventTopic.snsClient.publish(
      PublishRequest.builder()
        .topicArn(eventTopic.arn)
        .message(objectMapper.writeValueAsString(hmppsEvent))
        .eventTypeMessageAttributes(hmppsEvent.eventType)
        .build()
        .also { log.info("Published event $hmppsEvent to outbound topic") },
    ).get()
  }

  data class HmppsDomainEvent(
    val version: Int = 1,
    val eventType: String = EVENT_TYPE,
    val description: String = "A prisoner learning plan induction schedule created or amended",
    val detailUrl: String,
    val occurredAt: LocalDateTime,
    val personReference: PersonReference,
  ) {
    constructor(prisonerNumber: String, baseUrl: String, occurredAt: LocalDateTime) : this(
      detailUrl = URI.create("$baseUrl/inductions/$prisonerNumber/induction-schedule").toString(),
      personReference = PersonReference(identifiers = listOf(Identifier("NOMS", prisonerNumber))),
      occurredAt = occurredAt,
    )
  }
}
