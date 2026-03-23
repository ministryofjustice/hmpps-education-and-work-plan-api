package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.specialtests

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TEMPORARY_ABSENCE_RETURN
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RECEIVED_INTO_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.aValidHmppsDomainEventsSqsMessage
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.aValidPrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.EXEMPT_TEMP_ABSENCE
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.LocalDate
import java.time.OffsetDateTime

@Isolated
@ActiveProfiles("integration-test", "extend-exemption-deadline-always")
class PrisonerReceivedEventDueToTempAbsenceInductionScheduleTest : IntegrationTestBase() {
  companion object {
    private val today = LocalDate.now()
  }

  @Test
  fun `should add 5 days to the latest induction date and send outbound message when TAP Return event is processed given the induction was not overdue`() {
    // Given
    // an induction Schedule exists
    val prisonNumber = randomValidPrisonNumber()
    val earliestDateTime = OffsetDateTime.now()

    createInductionSchedule(
      prisonNumber,
      status = InductionScheduleStatus.SCHEDULED,
      deadlineDate = today.plusDays(1), // induction is not overdue
    )

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RECEIVED_INTO_PRISON,
      additionalInformation = aValidPrisonerReceivedAdditionalInformation(
        prisonNumber = prisonNumber,
        reason = TEMPORARY_ABSENCE_RETURN,
      ),
    )

    val expectedDeadlineDate = today.plusDays(5)

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
      .hasDeadlineDate(expectedDeadlineDate)
      .wasStatus(SCHEDULED)

    val inductionScheduleHistories = getInductionScheduleHistory(prisonNumber)
    assertThat(inductionScheduleHistories.inductionSchedules.size).isEqualTo(2)
    assertThat(inductionScheduleHistories.inductionSchedules[1].scheduleStatus).isEqualTo(EXEMPT_TEMP_ABSENCE)
    assertThat(inductionScheduleHistories.inductionSchedules[0].scheduleStatus).isEqualTo(SCHEDULED)

    val inductionScheduleEvents = inductionScheduleEventQueue.receiveEventsOnQueue(QueueType.INDUCTION)

    await.untilAsserted {
      assertThat(inductionScheduleEvents).hasSize(2)
      assertThat(inductionScheduleEvents).allSatisfy {
        assertThat(it.personReference.identifiers[0].value).isEqualTo(prisonNumber)
        assertThat(it.detailUrl).isEqualTo("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
      }
    }
  }

  @Test
  fun `should add 5 days to the latest induction date and send outbound message when TAP Return event is processed given the induction was already overdue`() {
    // Given
    // an induction Schedule exists
    val prisonNumber = randomValidPrisonNumber()
    val earliestDateTime = OffsetDateTime.now()

    createInductionSchedule(
      prisonNumber,
      status = InductionScheduleStatus.SCHEDULED,
      deadlineDate = today.minusDays(1), // induction is already overdue
    )

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RECEIVED_INTO_PRISON,
      additionalInformation = aValidPrisonerReceivedAdditionalInformation(
        prisonNumber = prisonNumber,
        reason = TEMPORARY_ABSENCE_RETURN,
      ),
    )

    val expectedDeadlineDate = today.plusDays(5)

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
      .hasDeadlineDate(expectedDeadlineDate)
      .wasStatus(SCHEDULED)

    val inductionScheduleHistories = getInductionScheduleHistory(prisonNumber)
    assertThat(inductionScheduleHistories.inductionSchedules.size).isEqualTo(2)
    assertThat(inductionScheduleHistories.inductionSchedules[1].scheduleStatus).isEqualTo(EXEMPT_TEMP_ABSENCE)
    assertThat(inductionScheduleHistories.inductionSchedules[0].scheduleStatus).isEqualTo(SCHEDULED)

    val inductionScheduleEvents = inductionScheduleEventQueue.receiveEventsOnQueue(QueueType.INDUCTION)

    await.untilAsserted {
      assertThat(inductionScheduleEvents).hasSize(2)
      assertThat(inductionScheduleEvents).allSatisfy {
        assertThat(it.personReference.identifiers[0].value).isEqualTo(prisonNumber)
        assertThat(it.detailUrl).isEqualTo("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
      }
    }
  }
}
