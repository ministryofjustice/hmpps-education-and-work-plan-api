package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.messaging

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.SqsMessage
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.util.UUID
import java.util.concurrent.TimeUnit

@ExtendWith(MockitoExtension::class)
class InboundEventsListenerTest : IntegrationTestBase() {

  @Test
  fun `should send message to service given message is a Notification message`() {
    // Given
    val prisonNumber = "A6099EA"
    val sqsMessage = SqsMessage(
      Type = "Notification",
      Message = """
        {
          "eventType": "prison-offender-events.prisoner.received",
          "personReference": { "identifiers": [ { "type": "NOMS", "value": "$prisonNumber" } ] },
          "occurredAt": "2024-08-08T09:07:55+01:00",
          "publishedAt": "2024-08-08T09:08:55.673395103+01:00",
          "description": "A prisoner has been received into prison",
          "version": "1.0",
          "additionalInformation": { "nomsNumber": "$prisonNumber", "reason": "ADMISSION", "details": "ACTIVE IN:ADM-N", "currentLocation": "IN_PRISON", "prisonId": "SWI", "nomisMovementReasonCode": "N", "currentPrisonStatus": "UNDER_PRISON_CARE" }
        }        
      """.trimIndent(),
      MessageId = UUID.randomUUID(),
    )

    // When
    sendDomainEvent(sqsMessage)

    // Then
    // wait until the message is processed
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    val result = await.atMost(10, TimeUnit.SECONDS).until {
      inductionScheduleRepository.findByPrisonNumber(prisonNumber) != null
    }

    val inductionSchedule = inductionScheduleRepository.findByPrisonNumber("A6099EA")
    checkNotNull(inductionSchedule) { "Expected a record with prisonNumber 'A6099EA' to exist in the database" }

    with(inductionSchedule) {
      assertThat(prisonNumber).isEqualTo("A6099EA")
      assertThat(scheduleCalculationRule).isEqualTo(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION)
    }
  }

  fun sendDomainEvent(
    message: SqsMessage,
    queueUrl: String = domainEventQueue.queueUrl,
  ): SendMessageResponse = domainEventQueueClient.sendMessage(
    SendMessageRequest.builder()
      .queueUrl(queueUrl)
      .messageBody(
        objectMapper.writeValueAsString(message),
      ).build(),
  ).get()
}
