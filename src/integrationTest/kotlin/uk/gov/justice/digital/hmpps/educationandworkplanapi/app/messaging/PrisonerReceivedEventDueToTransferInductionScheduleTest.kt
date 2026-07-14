package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TRANSFERRED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RECEIVED_INTO_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.COMPLETED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.OffsetDateTime

@Isolated
class PrisonerReceivedEventDueToTransferInductionScheduleTest : IntegrationTestBase() {

  companion object {
    private const val ORIGINAL_PRISON = "BXI"
    private const val PRISON_TRANSFERRING_TO = "MDI"
  }

  @Test
  fun `should not update Induction Schedule and send outbound message given 'prisoner received' (transfer) event for prisoner that has a completed induction`() {
    // Given
    // an induction Schedule exists
    val prisonNumber = randomValidPrisonNumber()
    val earliestDateTime = OffsetDateTime.now()

    createInductionSchedule(prisonNumber, status = InductionScheduleStatus.COMPLETED)

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RECEIVED_INTO_PRISON,
      additionalInformation = aValidPrisonerReceivedAdditionalInformation(
        prisonNumber = prisonNumber,
        prisonId = PRISON_TRANSFERRING_TO,
        reason = TRANSFERRED,
      ),
    )

    // When
    sendDomainEvent(sqsMessage)

    // Then
    // wait until the queue is drained / message is processed
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    val inductionSchedule = getInductionSchedule(prisonNumber)
    assertThat(inductionSchedule)
      .wasCreatedAtOrAfter(earliestDateTime)
      .wasUpdatedAtOrAfter(earliestDateTime)
      .wasCreatedBy("system")
      .wasCreatedByDisplayName("system")
      .wasCreatedAtPrison(ORIGINAL_PRISON)
      .wasUpdatedBy("system")
      .wasUpdatedByDisplayName("system")
      .wasUpdatedAtPrison(ORIGINAL_PRISON)
      .wasScheduleCalculationRule(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION)
      .wasStatus(COMPLETED)

    await.untilAsserted {
      val inductionScheduleHistories = getInductionScheduleHistory(prisonNumber)
      assertThat(inductionScheduleHistories.inductionSchedules.size).isEqualTo(0)
      assertThat(inductionScheduleEventQueue.countAllMessagesOnQueue()).isEqualTo(0)
    }
  }
}
