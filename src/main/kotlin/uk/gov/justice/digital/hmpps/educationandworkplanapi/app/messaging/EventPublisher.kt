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

@Component
class EventPublisher(
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
  @Value("\${service.base-url}") private val serviceBaseUrl: String,
) {

  internal val eventTopic by lazy { hmppsQueueService.findByTopicId("domainevents") as HmppsTopic }

  fun createAndPublishInductionEvent(prisonNumber: String, occurredAt: Instant = Instant.now()) {
    log.info { "Publishing induction schedule for prisoner [$prisonNumber]" }
    createAndPublishEvent(
      prisonNumber = prisonNumber,
      occurredAt = occurredAt,
      eventType = "plp.induction-schedule.updated",
      description = "A prisoner learning plan induction schedule created or amended",
      detailPath = "inductions/{prisonerNumber}/induction-schedule",
    )
  }

  fun createAndPublishReviewScheduleEvent(prisonNumber: String) {
    log.info { "Publishing review schedule for prisoner [$prisonNumber]" }
    createAndPublishEvent(
      prisonNumber = prisonNumber,
      occurredAt = Instant.now(),
      eventType = "plp.review-schedule.updated",
      description = "A prisoner learning plan review schedule created or amended",
      detailPath = "reviews/{prisonerNumber}/review-schedule",
    )
  }

  fun publishEvent(event: HmppsDomainEvent) {
    log.info("Publishing event of type ${event.eventType} for person reference ${event.personReference.identifiers}")
    eventTopic.snsClient.publish(
      PublishRequest.builder()
        .topicArn(eventTopic.arn)
        .message(objectMapper.writeValueAsString(event))
        .eventTypeMessageAttributes(event.eventType)
        .build()
        .also { log.info("Published event $event to outbound topic") },
    ).get()
  }

  fun createAndPublishEvent(
    prisonNumber: String,
    occurredAt: Instant,
    eventType: String,
    description: String,
    detailPath: String,
  ) {
    val event = HmppsDomainEvent(
      eventType = eventType,
      description = description,
      detailUrl = constructDetailUrl(detailPath, prisonNumber),
      occurredAt = occurredAt.atZone(ZoneId.of("Europe/London")).toLocalDateTime(),
      personReference = PersonReference(identifiers = listOf(Identifier("NOMS", prisonNumber))),
    )
    publishEvent(event)
  }

  private fun constructDetailUrl(detailPath: String, prisonerNumber: String): String {
    // Replace placeholder "{prisonerNumber}" with the actual prisonerNumber
    val updatedPath = detailPath.replace("{prisonerNumber}", prisonerNumber)
    return URI.create("$serviceBaseUrl/$updatedPath").toString()
  }

  data class HmppsDomainEvent(
    val version: Int = 1,
    val eventType: String,
    val description: String,
    val detailUrl: String,
    val occurredAt: LocalDateTime,
    val personReference: PersonReference,
  )

  data class PersonReference(
    val identifiers: List<Identifier>,
  )

  data class Identifier(
    val type: String,
    val value: String,
  )
}
