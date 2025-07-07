package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilAsserted
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.ADMISSION
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RECEIVED_INTO_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue

@Isolated
class PrisonerReceivedEventInvalidPrisonIdTest : IntegrationTestBase() {

  @Test
  fun `should create not create Induction Schedule given prisonId was invalid`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    with(aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "ZZGHI")) {
      createPrisonerAPIStub(prisonNumber, this)
    }

    assertThat(runCatching { getInduction(prisonNumber) }.getOrNull()).isNull()
    assertThat(runCatching { getInductionSchedule(prisonNumber) }.getOrNull()).isNull()

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RECEIVED_INTO_PRISON,
      additionalInformation = aValidPrisonerReceivedAdditionalInformation(
        prisonNumber = prisonNumber,
        reason = ADMISSION,
      ),
    )

    // When
    sendDomainEvent(sqsMessage)

    // Then
    // wait until the queue is drained / message is processed
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    await untilAsserted {
      // test induction schedule not created
      assertThat(inductionScheduleRepository.findByPrisonNumber(prisonNumber)).isNull()
    }
  }
}
