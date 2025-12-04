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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.OffsetDateTime

@Isolated
class PrisonerReceivedDueToAdmissionEventDuringChristmasTest : IntegrationTestBase() {

  @Test
  fun `should create new Induction Schedule given prisoner does not already have an Induction`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    with(aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "MDI")) {
      createPrisonerAPIStub(prisonNumber, this)
    }

    assertThat(runCatching { getInduction(prisonNumber) }.getOrNull()).isNull()
    assertThat(runCatching { getInductionSchedule(prisonNumber) }.getOrNull()).isNull()

    val earliestDateTime = OffsetDateTime.now()

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
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    await untilAsserted {
      val inductionSchedule = getInductionSchedule(prisonNumber)
      assertThat(inductionSchedule)
        .wasCreatedAtOrAfter(earliestDateTime)
        .wasUpdatedAtOrAfter(earliestDateTime)
        .wasCreatedBy("system")
        .wasCreatedByDisplayName("system")
        .wasCreatedAtPrison("MDI")
        .wasUpdatedBy("system")
        .wasUpdatedByDisplayName("system")
        .wasUpdatedAtPrison("MDI")
        .wasScheduleCalculationRule(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION) // TODO NEW_PRISON_ADMISSION_EXTENDED_DEADLINE_PERIOD
        .wasStatus(SCHEDULED)

      val inductionScheduleHistories = getInductionScheduleHistory(prisonNumber)
      assertThat(inductionScheduleHistories)
        .hasNumberOfInductionScheduleVersions(1)
        .inductionScheduleVersion(1) {
          it.wasCreatedAtOrAfter(earliestDateTime)
            .wasUpdatedAtOrAfter(earliestDateTime)
            .wasCreatedBy("system")
            .wasCreatedByDisplayName("system")
            .wasCreatedAtPrison("MDI")
            .wasUpdatedBy("system")
            .wasUpdatedByDisplayName("system")
            .wasUpdatedAtPrison("MDI")
            .wasScheduleCalculationRule(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION) // TODO NEW_PRISON_ADMISSION_EXTENDED_DEADLINE_PERIOD
            .wasStatus(SCHEDULED)
            .wasVersion(1)
        }

      val inductionScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.INDUCTION)
      assertThat(inductionScheduleEvent.personReference.identifiers[0].value).isEqualTo(prisonNumber)
      assertThat(inductionScheduleEvent.detailUrl).isEqualTo("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
    }
  }
}
