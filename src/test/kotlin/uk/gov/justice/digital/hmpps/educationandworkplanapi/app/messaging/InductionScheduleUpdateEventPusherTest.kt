package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class InductionScheduleUpdateEventPusherTest {
  private val hmppsQueueService: HmppsQueueService = mock()
  private val snsClient: SnsAsyncClient = mock()
  private val objectMapper: ObjectMapper = mock()
  private val service = InductionScheduleUpdateEventPusher(hmppsQueueService, objectMapper, "http://localhost:8080")

  @Test
  fun `send event converts to induction schedule event update event`() {
    whenever(objectMapper.writeValueAsString(any())).thenReturn("messageAsJson")
    whenever(hmppsQueueService.findByTopicId("domainevents")).thenReturn(HmppsTopic("id", "topicUrn", snsClient))

    val publishResponse = PublishResponse.builder().messageId("123").build()
    val completableFuture = CompletableFuture<PublishResponse>()
    completableFuture.complete(publishResponse)
    val occurredAt = Instant.now()

    whenever(snsClient.publish(any<PublishRequest>())).thenReturn(completedFuture(publishResponse))
    service.sendEvent("A1234AC", occurredAt)
    verify(objectMapper).writeValueAsString(
      check<InductionScheduleUpdateEventPusher.HmppsDomainEvent> {
        assertThat(it).isEqualTo(
          InductionScheduleUpdateEventPusher.HmppsDomainEvent(
            eventType = "plp.induction-schedule.updated",
            detailUrl = "http://localhost:8080/inductions/A1234AC/induction-schedule",
            occurredAt = occurredAt
              .atZone(ZoneId.of("Europe/London")).toLocalDateTime(),
            personReference = PersonReference(identifiers = listOf(Identifier("NOMS", "A1234AC"))),
          ),
        )
      },
    )
  }

  @Test
  fun `send event sends to the sns client`() {
    whenever(objectMapper.writeValueAsString(any())).thenReturn("messageAsJson")
    whenever(hmppsQueueService.findByTopicId("domainevents")).thenReturn(HmppsTopic("id", "topicArn", snsClient))
    val publishResponse = mock<PublishResponse>()
    whenever(snsClient.publish(any<PublishRequest>())).thenReturn(completedFuture(publishResponse))
    val occurredAt = Instant.now()

    service.sendEvent("A1234AC", occurredAt)
    verify(snsClient).publish(
      PublishRequest.builder().message("messageAsJson")
        .topicArn("topicArn")
        .messageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue.builder().dataType("String")
              .stringValue("plp.induction-schedule.updated")
              .build(),
            "contentType" to MessageAttributeValue.builder().dataType("String").stringValue("text/plain;charset=UTF-8")
              .build(),
          ),
        )
        .build(),
    )
  }
}
