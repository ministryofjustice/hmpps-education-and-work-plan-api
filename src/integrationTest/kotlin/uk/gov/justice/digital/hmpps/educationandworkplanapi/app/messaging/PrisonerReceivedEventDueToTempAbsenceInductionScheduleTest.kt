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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.events.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.COMPLETED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.EXEMPT_PRISON_REGIME_CIRCUMSTANCES
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.EXEMPT_TEMP_ABSENCE
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.LocalDate
import java.time.OffsetDateTime

@Isolated
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
    assertThat(inductionScheduleHistories)
      .hasNumberOfInductionScheduleVersions(2)
      .inductionScheduleVersion(1) { it.wasStatus(EXEMPT_TEMP_ABSENCE) }
      .inductionScheduleVersion(2) { it.wasStatus(SCHEDULED) }

    val inductionScheduleEvents = inductionScheduleEventQueue.receiveEventsOnQueue(QueueType.INDUCTION)

    await.untilAsserted {
      assertThat(inductionScheduleEvents)
        .hasNumberOfEvents(2)
        .allEvents {
          it.hasDetailUrl("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
            .hasNumberOfPersonReferenceIdentifiers(1)
            .personReferenceIdentifier(1) { it.hasValue(prisonNumber) }
        }
    }
  }

  @Test
  fun `should not add 5 days to the latest induction date and send outbound message when TAP Return event is processed given the induction was already overdue`() {
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

    val expectedDeadlineDate = today.minusDays(1)

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
    assertThat(inductionScheduleHistories)
      .hasNumberOfInductionScheduleVersions(2)
      .inductionScheduleVersion(1) { it.wasStatus(EXEMPT_TEMP_ABSENCE) }
      .inductionScheduleVersion(2) { it.wasStatus(SCHEDULED) }

    val inductionScheduleEvents = inductionScheduleEventQueue.receiveEventsOnQueue(QueueType.INDUCTION)

    await.untilAsserted {
      assertThat(inductionScheduleEvents)
        .hasNumberOfEvents(2)
        .allEvents {
          it.hasDetailUrl("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
            .hasNumberOfPersonReferenceIdentifiers(1)
            .personReferenceIdentifier(1) { it.hasValue(prisonNumber) }
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
      assertThat(inductionScheduleHistories).hasNumberOfInductionScheduleVersions(0)
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
      assertThat(inductionScheduleHistories).hasNumberOfInductionScheduleVersions(0)
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
      .hasDeadlineDate(today.plusDays(10)) // PES rules for new admissions are today + 10 days
      .wasStatus(SCHEDULED)

    val inductionScheduleHistories = getInductionScheduleHistory(prisonNumber)
    assertThat(inductionScheduleHistories)
      .hasNumberOfInductionScheduleVersions(4)
      .inductionScheduleVersion(1) { it.wasStatus(PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS) }
      .inductionScheduleVersion(2) { it.wasStatus(SCHEDULED) }
      .inductionScheduleVersion(3) { it.wasStatus(EXEMPT_TEMP_ABSENCE) }
      .inductionScheduleVersion(4) { it.wasStatus(SCHEDULED) }

    val inductionScheduleEvents = inductionScheduleEventQueue.receiveEventsOnQueue(QueueType.INDUCTION)

    await.untilAsserted {
      assertThat(inductionScheduleEvents)
        .hasNumberOfEvents(4)
        .allEvents {
          it.hasDetailUrl("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
            .hasNumberOfPersonReferenceIdentifiers(1)
            .personReferenceIdentifier(1) { it.hasValue(prisonNumber) }
        }
    }
  }

  @Test
  fun `should update Induction Schedule and send outbound message given 'prisoner received' (temp absence) event for prisoner that has an active Induction Schedule and status is EXEMPT_PRISONER_RELEASE`() {
    // Given
    // an induction Schedule exists
    val prisonNumber = randomValidPrisonNumber()
    val earliestDateTime = OffsetDateTime.now()

    createInductionSchedule(prisonNumber, status = InductionScheduleStatus.EXEMPT_PRISONER_RELEASE, deadlineDate = today.plusDays(1))

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
      .hasDeadlineDate(today.plusDays(5))
      .wasStatus(SCHEDULED)

    val inductionScheduleHistories = getInductionScheduleHistory(prisonNumber)
    assertThat(inductionScheduleHistories)
      .hasNumberOfInductionScheduleVersions(2)
      .inductionScheduleVersion(1) { it.wasStatus(EXEMPT_TEMP_ABSENCE) }
      .inductionScheduleVersion(2) { it.wasStatus(SCHEDULED) }

    val inductionScheduleEvents = inductionScheduleEventQueue.receiveEventsOnQueue(QueueType.INDUCTION)

    await.untilAsserted {
      assertThat(inductionScheduleEvents)
        .hasNumberOfEvents(2)
        .allEvents {
          it.hasDetailUrl("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
            .hasNumberOfPersonReferenceIdentifiers(1)
            .personReferenceIdentifier(1) { it.hasValue(prisonNumber) }
        }
    }
  }
}
