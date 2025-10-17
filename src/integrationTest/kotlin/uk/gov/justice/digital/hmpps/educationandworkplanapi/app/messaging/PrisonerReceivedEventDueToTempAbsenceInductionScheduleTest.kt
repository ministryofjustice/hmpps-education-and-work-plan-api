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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TEMPORARY_ABSENCE_RETURN
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RECEIVED_INTO_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.COMPLETED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.EXEMPT_PRISON_REGIME_CIRCUMSTANCES
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.EXEMPT_TEMP_ABSENCE
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.LocalDate
import java.time.OffsetDateTime

@Isolated
class PrisonerReceivedEventDueToTempAbsenceInductionScheduleTest : IntegrationTestBase() {

  @Test
  fun `should update Induction Schedule and send outbound message given 'prisoner received' (temp absence) event for prisoner that has an active Induction Schedule`() {
    // Given
    // an induction Schedule exists
    val prisonNumber = randomValidPrisonNumber()
    val earliestDateTime = OffsetDateTime.now()

    createInductionSchedule(prisonNumber, status = InductionScheduleStatus.SCHEDULED, deadlineDate = LocalDate.now().plusDays(1))

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
      .hasDeadlineDate(LocalDate.now().plusDays(5))
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
  fun `should not update Induction Schedule and send outbound message given 'prisoner received' (temp absence) event for prisoner that has a completed induction`() {
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
      .wasStatus(COMPLETED)

    await.untilAsserted {
      val inductionScheduleHistories = getInductionScheduleHistory(prisonNumber)
      assertThat(inductionScheduleHistories.inductionSchedules.size).isEqualTo(0)
      assertThat(inductionScheduleEventQueue.countAllMessagesOnQueue()).isEqualTo(0)
    }
  }

  @Test
  fun `should not update Induction Schedule and send outbound message given 'prisoner received' (temp absence) event for prisoner that has manual exempt induction`() {
    // Given
    // an induction Schedule exists
    val prisonNumber = randomValidPrisonNumber()
    val earliestDateTime = OffsetDateTime.now()

    createInductionSchedule(prisonNumber, status = InductionScheduleStatus.EXEMPT_PRISON_REGIME_CIRCUMSTANCES)

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
      .wasStatus(EXEMPT_PRISON_REGIME_CIRCUMSTANCES)

    await.untilAsserted {
      val inductionScheduleHistories = getInductionScheduleHistory(prisonNumber)
      assertThat(inductionScheduleHistories.inductionSchedules.size).isEqualTo(0)
      assertThat(inductionScheduleEventQueue.countAllMessagesOnQueue()).isEqualTo(0)
    }
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
      .hasDeadlineDate(LocalDate.now().plusDays(20))
      .wasStatus(SCHEDULED)

    val inductionScheduleHistories = getInductionScheduleHistory(prisonNumber)
    assertThat(inductionScheduleHistories.inductionSchedules.size).isEqualTo(3)
    assertThat(inductionScheduleHistories.inductionSchedules[2].scheduleStatus).isEqualTo(SCHEDULED)
    assertThat(inductionScheduleHistories.inductionSchedules[1].scheduleStatus).isEqualTo(EXEMPT_TEMP_ABSENCE)
    assertThat(inductionScheduleHistories.inductionSchedules[0].scheduleStatus).isEqualTo(SCHEDULED)

    val inductionScheduleEvents = inductionScheduleEventQueue.receiveEventsOnQueue(QueueType.INDUCTION)

    await.untilAsserted {
      assertThat(inductionScheduleEvents).hasSize(3)
      assertThat(inductionScheduleEvents).allSatisfy {
        assertThat(it.personReference.identifiers[0].value).isEqualTo(prisonNumber)
        assertThat(it.detailUrl).isEqualTo("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
      }
    }
  }

  @Test
  fun `should update Induction Schedule and send outbound message given 'prisoner received' (temp absence) event for prisoner that has an active Induction Schedule and status is EXEMPT_PRISONER_RELEASE`() {
    // Given
    // an induction Schedule exists
    val prisonNumber = randomValidPrisonNumber()
    val earliestDateTime = OffsetDateTime.now()

    createInductionSchedule(prisonNumber, status = InductionScheduleStatus.EXEMPT_PRISONER_RELEASE, deadlineDate = LocalDate.now().plusDays(1))

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
      .hasDeadlineDate(LocalDate.now().plusDays(5))
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
