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
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.LegalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.SqsMessage
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.INDUCTIONS_RO
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule as InductionScheduleCalculationRuleResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus as InductionScheduleStatusResponse

@ExtendWith(MockitoExtension::class)
class InboundEventsListenerTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/inductions/{prisonNumber}/induction-schedule"
  }

  @Test
  fun `should send message to service given message is a Notification message`() {
    // Given
    val initialDateTime = OffsetDateTime.now()
    val inductionScheduleBefore = inductionScheduleRepository.findByPrisonNumber("A6099EA")
    assertThat(inductionScheduleBefore).isNull()

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

    await.atMost(10, TimeUnit.SECONDS).until {
      inductionScheduleRepository.findByPrisonNumber(prisonNumber) != null
    }

    val inductionSchedule = inductionScheduleRepository.findByPrisonNumber("A6099EA")
    checkNotNull(inductionSchedule) { "Expected a record with prisonNumber 'A6099EA' to exist in the database" }

    with(inductionSchedule) {
      assertThat(prisonNumber).isEqualTo("A6099EA")
      assertThat(scheduleCalculationRule).isEqualTo(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION)
    }

    // test that outbound event is also created:
    val inductionScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.INDUCTION)
    assertThat(inductionScheduleEvent.personReference.identifiers[0].value).isEqualTo("A6099EA")
    assertThat(inductionScheduleEvent.detailUrl).isEqualTo("http://localhost:8080/inductions/A6099EA/induction-schedule")

    // also test that the endpoint returns an induction schedule
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(
        aValidTokenWithAuthority(
          INDUCTIONS_RO,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(InductionScheduleResponse::class.java)

    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .wasCreatedAfter(initialDateTime)
      .wasUpdatedAfter(initialDateTime)
      .wasCreatedBy("system")
      .wasCreatedByDisplayName("system")
      .wasUpdatedBy("system")
      .wasUpdatedByDisplayName("system")
      .wasScheduleCalculationRule(InductionScheduleCalculationRuleResponse.NEW_PRISON_ADMISSION)
      .wasStatus(InductionScheduleStatusResponse.SCHEDULED)
  }

  @Test
  fun `receiving an admission Event when an induction exists creates a review message message`() {
    // Given
    // an induction exists:
    val prisonNumber = randomValidPrisonNumber()
    createInductionSchedule(
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.COMPLETE,
    )
    createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork())

    val expectedPrisoner = Prisoner(
      prisonerNumber = prisonNumber,
      legalStatus = LegalStatus.SENTENCED,
      releaseDate = LocalDate.now().plusYears(1),
      prisonId = "BXI",

    )
    createPrisonerAPIStub(prisonNumber, expectedPrisoner)

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

    await.atMost(10, TimeUnit.SECONDS).until {
      reviewScheduleRepository.findActiveReviewSchedule(prisonNumber) != null
    }

    // test that outbound event is also created:
    val reviewScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.REVIEW)
    assertThat(reviewScheduleEvent.personReference.identifiers[0].value).isEqualTo(prisonNumber)
    assertThat(reviewScheduleEvent.detailUrl).isEqualTo("http://localhost:8080/reviews/$prisonNumber/review-schedule")
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
