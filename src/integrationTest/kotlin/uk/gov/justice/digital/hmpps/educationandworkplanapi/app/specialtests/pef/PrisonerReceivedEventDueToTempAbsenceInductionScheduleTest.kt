package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.specialtests.pef

import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TEMPORARY_ABSENCE_RETURN
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RECEIVED_INTO_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.aValidHmppsDomainEventsSqsMessage
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.aValidPrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.events.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.EXEMPT_TEMP_ABSENCE
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.LocalDate
import java.time.OffsetDateTime

@Isolated
@ActiveProfiles("integration-test", "ciag-kpi-pef-rules")
class PrisonerReceivedEventDueToTempAbsenceInductionScheduleTest : IntegrationTestBase() {
  companion object {
    private val today = LocalDate.now()
  }

  // NOTE: this test is to test the scenario where we did not get the admission message for this person.
  // so for all intents and purposes this is the admission message
  @Test
  fun `should update Induction Schedule and send outbound message given 'prisoner received' (temp absence) event for prisoner that has no induction schedule`() {
    // Given
    // an induction Schedule exists
    val prisonNumber = randomValidPrisonNumber()
    val earliestDateTime = OffsetDateTime.now()

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RECEIVED_INTO_PRISON,
      additionalInformation = aValidPrisonerReceivedAdditionalInformation(
        prisonNumber = prisonNumber,
        reason = TEMPORARY_ABSENCE_RETURN,
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
      .wasUpdatedBy("system")
      .wasUpdatedByDisplayName("system")
      .wasScheduleCalculationRule(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION)
      // basically this message was treat like a new admission
      .hasDeadlineDate(today.plusDays(20)) // PEF rules for new admissions are today + 20 days
      .wasStatus(SCHEDULED)

    val inductionScheduleHistories = getInductionScheduleHistory(prisonNumber)
    assertThat(inductionScheduleHistories)
      .hasNumberOfInductionScheduleVersions(3)
      .inductionScheduleVersion(1) { it.wasStatus(SCHEDULED) }
      .inductionScheduleVersion(2) { it.wasStatus(EXEMPT_TEMP_ABSENCE) }
      .inductionScheduleVersion(3) { it.wasStatus(SCHEDULED) }

    val inductionScheduleEvents = inductionScheduleEventQueue.receiveEventsOnQueue(QueueType.INDUCTION)

    await.untilAsserted {
      assertThat(inductionScheduleEvents)
        .hasNumberOfEvents(3)
        .allEvents {
          it.hasDetailUrl("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
            .hasNumberOfPersonReferenceIdentifiers(1)
            .personReferenceIdentifier(1) { it.hasValue(prisonNumber) }
        }
    }
  }
}
