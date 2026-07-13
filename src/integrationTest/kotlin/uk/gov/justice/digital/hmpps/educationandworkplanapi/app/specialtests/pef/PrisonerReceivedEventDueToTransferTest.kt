package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.specialtests.pef

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TRANSFERRED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RECEIVED_INTO_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.aValidHmppsDomainEventsSqsMessage
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.aValidPrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.LocalDate
import java.util.*
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus as InductionScheduleStatusApi

@Isolated
@ActiveProfiles("integration-test", "ciag-kpi-pef-rules")
class PrisonerReceivedEventDueToTransferTest : IntegrationTestBase() {

  companion object {
    private const val ORIGINAL_PRISON = "BXI"
    private const val PRISON_TRANSFERRING_TO = "MDI"
  }

  @Test
  fun `should update Review Schedule and send outbound message given 'prisoner received' (transfer) event for prisoner that has an active Review Schedule and does not have completed Screenings and Assessments`() {
    // Given
    // an induction and action plan are created. This will have created the initial Review Schedule with the status SCHEDULED
    val prisonNumber = setUpRandomPrisoner()
    createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork(prisonId = ORIGINAL_PRISON))
    createActionPlan(prisonNumber)

    // Set the review dates so that we can assert that they've been changed
    updateReviewScheduleReviewDates(
      prisonNumber = prisonNumber,
      earliestReviewDate = LocalDate.now().plusMonths(1),
      latestReviewDate = LocalDate.now().plusMonths(6),
    )

    val expectedEarliestReviewDate = LocalDate.now()
    val expectedLatestReviewDate = LocalDate.now().plusDays(10)

    // The above calls set the data up but they will also generate events so clear these out before starting the test.
    // Before clearing the queues though we need to wait until the "plp.review-schedule.updated" event on the REVIEW queue is received.
    await untilCallTo {
      reviewScheduleEventQueue.countAllMessagesOnQueue()
    } matches { it != null && it > 0 }
    clearQueues()

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

    val reviewSchedules = getReviewSchedules(prisonNumber)
    assertThat(reviewSchedules)
      // Expect 3 schedules - 1 is the initial scheduled, 2 is exemption due to transfer, 3 is the re-scheduled
      .hasNumberOfReviewSchedules(3)
      .reviewScheduleAtVersion(2) {
        // Review Schedule version 2 is the exempted schedule due to transfer
        it.hasStatus(ReviewScheduleStatus.EXEMPT_PRISONER_TRANSFER)
          .wasUpdatedAtPrison(ORIGINAL_PRISON)
      }
      .reviewScheduleAtVersion(3) {
        // Review Schedule version 3 is the re-scheduled review schedule
        it.hasStatus(ReviewScheduleStatus.SCHEDULED)
          .wasUpdatedAtPrison(PRISON_TRANSFERRING_TO)
          .hasReviewDateFrom(expectedEarliestReviewDate)
          .hasReviewDateTo(expectedLatestReviewDate)
      }

    // test that outbound events are also created (there would have been 3 in total, but we cleared the queue after the first one in the given block above). The 2 new ones are the ones we are really interested in though)
    val reviewScheduleEvents = inductionScheduleEventQueue.receiveEventsOnQueue(QueueType.REVIEW)
    assertThat(reviewScheduleEvents).hasSize(2)
    assertThat(reviewScheduleEvents).allSatisfy {
      assertThat(it.personReference.identifiers[0].value).isEqualTo(prisonNumber)
      assertThat(it.detailUrl).isEqualTo("http://localhost:8080/reviews/$prisonNumber/review-schedule")
    }
  }

  @Test
  fun `should update Review Schedule and send outbound message given 'prisoner received' (transfer) event for prisoner that has an active Review Schedule and already has completed Screenings and Assessments`() {
    // Given
    // an induction and action plan are created. This will have created the initial Review Schedule with the status SCHEDULED
    val prisonNumber = setUpRandomPrisoner()
    createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork(prisonId = ORIGINAL_PRISON))
    createActionPlan(prisonNumber)

    educationAssessmentEventRepository.save(
      EducationAssessmentEventEntity(
        reference = UUID.randomUUID(),
        prisonNumber = prisonNumber,
        status = ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.now().minusDays(2),
        source = "CURIOUS",
        detailUrl = null,
        createdAtPrison = "BXI",
        updatedAtPrison = "BXI",
      ),
    )

    // Set the review dates so that we can assert that they've been changed
    updateReviewScheduleReviewDates(
      prisonNumber = prisonNumber,
      earliestReviewDate = LocalDate.now().plusMonths(1),
      latestReviewDate = LocalDate.now().plusMonths(6),
    )

    val expectedEarliestReviewDate = LocalDate.now()
    val expectedLatestReviewDate = LocalDate.now().plusDays(10)

    // The above calls set the data up but they will also generate events so clear these out before starting the test.
    // Before clearing the queues though we need to wait until the "plp.review-schedule.updated" event on the REVIEW queue is received.
    await untilCallTo {
      reviewScheduleEventQueue.countAllMessagesOnQueue()
    } matches { it != null && it > 0 }
    clearQueues()

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

    val reviewSchedules = getReviewSchedules(prisonNumber)
    assertThat(reviewSchedules)
      // Expect 3 schedules - 1 is the initial scheduled, 2 is exemption due to transfer, 3 is the re-scheduled
      .hasNumberOfReviewSchedules(3)
      .reviewScheduleAtVersion(2) {
        // Review Schedule version 2 is the exempted schedule due to transfer
        it.hasStatus(ReviewScheduleStatus.EXEMPT_PRISONER_TRANSFER)
          .wasUpdatedAtPrison(ORIGINAL_PRISON)
      }
      .reviewScheduleAtVersion(3) {
        // Review Schedule version 3 is the re-scheduled review schedule
        it.hasStatus(ReviewScheduleStatus.SCHEDULED)
          .wasUpdatedAtPrison(PRISON_TRANSFERRING_TO)
          .hasReviewDateFrom(expectedEarliestReviewDate)
          .hasReviewDateTo(expectedLatestReviewDate)
      }

    // test that outbound events are also created (there would have been 3 in total, but we cleared the queue after the first one in the given block above). The 2 new ones are the ones we are really interested in though)
    val reviewScheduleEvents = inductionScheduleEventQueue.receiveEventsOnQueue(QueueType.REVIEW)
    assertThat(reviewScheduleEvents).hasSize(2)
    assertThat(reviewScheduleEvents).allSatisfy {
      assertThat(it.personReference.identifiers[0].value).isEqualTo(prisonNumber)
      assertThat(it.detailUrl).isEqualTo("http://localhost:8080/reviews/$prisonNumber/review-schedule")
    }
  }

  @Test
  fun `should update Induction Schedule and send outbound message given 'prisoner received' (transfer) event for prisoner that has a SCHEDULED induction and does not have completed Screenings and Assessments`() {
    // Given
    // an induction is scheduled
    val prisonNumber = setUpRandomPrisoner()
    val inductionScheduleReference = UUID.randomUUID()
    createInductionSchedule(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.SCHEDULED,
      deadlineDate = LocalDate.now(),
    )
    createInductionScheduleHistory(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.SCHEDULED,
      deadlineDate = LocalDate.now(),
    )

    val expectedDeadlineDate = LocalDate.now().plusDays(20)

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

    val inductionSchedules = getInductionScheduleHistory(prisonNumber)
    assertThat(inductionSchedules)
      // Expect 3 schedules - 1 is the initial scheduled, 2 is exemption due to transfer, 3 is the rescheduled
      .hasNumberOfInductionScheduleVersions(3)
      .inductionScheduleVersion(1) {
        // Induction Schedule version 1 is the original schedule
        it.wasStatus(InductionScheduleStatusApi.SCHEDULED)
          .wasUpdatedAtPrison(ORIGINAL_PRISON)
      }
      .inductionScheduleVersion(2) {
        // Induction Schedule version 2 is the exempted schedule due to transfer
        it.wasStatus(InductionScheduleStatusApi.EXEMPT_PRISONER_TRANSFER)
          .wasUpdatedAtPrison(ORIGINAL_PRISON)
      }
      .inductionScheduleVersion(3) {
        // Induction Schedule version 3 is the re-scheduled induction schedule
        it.wasStatus(InductionScheduleStatusApi.SCHEDULED)
          .wasUpdatedAtPrison(PRISON_TRANSFERRING_TO)
          .hasDeadlineDate(expectedDeadlineDate)
      }

    // test that outbound events are also created.
    val inductionScheduleEvents = inductionScheduleEventQueue.receiveEventsOnQueue(QueueType.INDUCTION)
    assertThat(inductionScheduleEvents).hasSize(2)
    assertThat(inductionScheduleEvents).allSatisfy {
      assertThat(it.personReference.identifiers[0].value).isEqualTo(prisonNumber)
      assertThat(it.detailUrl).isEqualTo("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
    }
  }

  @Test
  fun `should update Induction Schedule and send outbound message given 'prisoner received' (transfer) event for prisoner that has a SCHEDULED induction and already has completed Screenings and Assessments`() {
    // Given
    // an induction is scheduled
    val prisonNumber = setUpRandomPrisoner()
    val inductionScheduleReference = UUID.randomUUID()
    createInductionSchedule(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.SCHEDULED,
      deadlineDate = LocalDate.now(),
    )
    createInductionScheduleHistory(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.SCHEDULED,
      deadlineDate = LocalDate.now(),
    )

    educationAssessmentEventRepository.save(
      EducationAssessmentEventEntity(
        reference = UUID.randomUUID(),
        prisonNumber = prisonNumber,
        status = ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.now().minusDays(2),
        source = "CURIOUS",
        detailUrl = null,
        createdAtPrison = "BXI",
        updatedAtPrison = "BXI",
      ),
    )

    val expectedDeadlineDate = LocalDate.now().plusDays(20)

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

    val inductionSchedules = getInductionScheduleHistory(prisonNumber)
    assertThat(inductionSchedules)
      // Expect 3 schedules - 1 is the initial scheduled, 2 is exemption due to transfer, 3 is the rescheduled
      .hasNumberOfInductionScheduleVersions(3)
      .inductionScheduleVersion(1) {
        // Induction Schedule version 1 is the original schedule
        it.wasStatus(InductionScheduleStatusApi.SCHEDULED)
          .wasUpdatedAtPrison(ORIGINAL_PRISON)
      }
      .inductionScheduleVersion(2) {
        // Induction Schedule version 2 is the exempted schedule due to transfer
        it.wasStatus(InductionScheduleStatusApi.EXEMPT_PRISONER_TRANSFER)
          .wasUpdatedAtPrison(ORIGINAL_PRISON)
      }
      .inductionScheduleVersion(3) {
        // Induction Schedule version 3 is the re-scheduled induction schedule
        it.wasStatus(InductionScheduleStatusApi.SCHEDULED)
          .wasUpdatedAtPrison(PRISON_TRANSFERRING_TO)
          .hasDeadlineDate(expectedDeadlineDate)
      }

    // test that outbound events are also created.
    val inductionScheduleEvents = inductionScheduleEventQueue.receiveEventsOnQueue(QueueType.INDUCTION)
    assertThat(inductionScheduleEvents).hasSize(2)
    assertThat(inductionScheduleEvents).allSatisfy {
      assertThat(it.personReference.identifiers[0].value).isEqualTo(prisonNumber)
      assertThat(it.detailUrl).isEqualTo("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
    }
  }

  @Test
  fun `should update Induction Schedule and send outbound message given 'prisoner received' (transfer) event for prisoner that has an exempt induction and does not have completed Screenings and Assessments`() {
    // Given
    // an induction is scheduled
    val prisonNumber = setUpRandomPrisoner()
    val inductionScheduleReference = UUID.randomUUID()
    createInductionSchedule(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.EXEMPT_PRISONER_SAFETY_ISSUES,
      deadlineDate = LocalDate.now(),
    )
    createInductionScheduleHistory(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.SCHEDULED,
      deadlineDate = LocalDate.now(),
      version = 1,
    )
    createInductionScheduleHistory(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.EXEMPT_PRISONER_SAFETY_ISSUES,
      deadlineDate = LocalDate.now(),
      version = 2,
    )

    val expectedDeadlineDate = LocalDate.now().plusDays(20)

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

    val inductionSchedules = getInductionScheduleHistory(prisonNumber)
    assertThat(inductionSchedules)
      // Expect 4 schedules - 1 is the initial scheduled, 2 is exemption, 4 is exemption due to transfer, 4 is the rescheduled
      .hasNumberOfInductionScheduleVersions(4)
      .inductionScheduleVersion(1) {
        // Induction Schedule version 1 is the original schedule
        it.wasStatus(InductionScheduleStatusApi.SCHEDULED)
          .wasUpdatedAtPrison(ORIGINAL_PRISON)
      }
      .inductionScheduleVersion(2) {
        // Induction Schedule version 2 is the original exemption
        it.wasStatus(InductionScheduleStatusApi.EXEMPT_PRISONER_SAFETY_ISSUES)
          .wasUpdatedAtPrison(ORIGINAL_PRISON)
      }
      .inductionScheduleVersion(3) {
        // Induction Schedule version 3 is the exempted schedule due to transfer
        it.wasStatus(InductionScheduleStatusApi.EXEMPT_PRISONER_TRANSFER)
          .wasUpdatedAtPrison(ORIGINAL_PRISON)
      }.inductionScheduleVersion(4) {
        // Induction Schedule version 4 is the re-scheduled induction schedule
        it.wasStatus(InductionScheduleStatusApi.SCHEDULED)
          .wasUpdatedAtPrison(PRISON_TRANSFERRING_TO)
          .hasDeadlineDate(expectedDeadlineDate)
      }

    // test that outbound events are also created.
    val inductionScheduleEvents = inductionScheduleEventQueue.receiveEventsOnQueue(QueueType.INDUCTION)
    assertThat(inductionScheduleEvents).hasSize(2)
    assertThat(inductionScheduleEvents).allSatisfy {
      assertThat(it.personReference.identifiers[0].value).isEqualTo(prisonNumber)
      assertThat(it.detailUrl).isEqualTo("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
    }
  }

  @Test
  fun `should update Induction Schedule and send outbound message given 'prisoner received' (transfer) event for prisoner that has an exempt induction and already has completed Screenings and Assessments`() {
    // Given
    // an induction is scheduled
    val prisonNumber = setUpRandomPrisoner()
    val inductionScheduleReference = UUID.randomUUID()
    createInductionSchedule(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.EXEMPT_PRISONER_SAFETY_ISSUES,
      deadlineDate = LocalDate.now(),
    )
    createInductionScheduleHistory(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.SCHEDULED,
      deadlineDate = LocalDate.now(),
      version = 1,
    )
    createInductionScheduleHistory(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.EXEMPT_PRISONER_SAFETY_ISSUES,
      deadlineDate = LocalDate.now(),
      version = 2,
    )

    educationAssessmentEventRepository.save(
      EducationAssessmentEventEntity(
        reference = UUID.randomUUID(),
        prisonNumber = prisonNumber,
        status = ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.now().minusDays(2),
        source = "CURIOUS",
        detailUrl = null,
        createdAtPrison = "BXI",
        updatedAtPrison = "BXI",
      ),
    )

    val expectedDeadlineDate = LocalDate.now().plusDays(20)

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

    val inductionSchedules = getInductionScheduleHistory(prisonNumber)
    assertThat(inductionSchedules)
      // Expect 4 schedules - 1 is the initial scheduled, 2 is exemption, 4 is exemption due to transfer, 4 is the rescheduled
      .hasNumberOfInductionScheduleVersions(4)
      .inductionScheduleVersion(1) {
        // Induction Schedule version 1 is the original schedule
        it.wasStatus(InductionScheduleStatusApi.SCHEDULED)
          .wasUpdatedAtPrison(ORIGINAL_PRISON)
      }
      .inductionScheduleVersion(2) {
        // Induction Schedule version 2 is the original exemption
        it.wasStatus(InductionScheduleStatusApi.EXEMPT_PRISONER_SAFETY_ISSUES)
          .wasUpdatedAtPrison(ORIGINAL_PRISON)
      }
      .inductionScheduleVersion(3) {
        // Induction Schedule version 3 is the exempted schedule due to transfer
        it.wasStatus(InductionScheduleStatusApi.EXEMPT_PRISONER_TRANSFER)
          .wasUpdatedAtPrison(ORIGINAL_PRISON)
      }.inductionScheduleVersion(4) {
        // Induction Schedule version 4 is the re-scheduled induction schedule
        it.wasStatus(InductionScheduleStatusApi.SCHEDULED)
          .wasUpdatedAtPrison(PRISON_TRANSFERRING_TO)
          .hasDeadlineDate(expectedDeadlineDate)
      }

    // test that outbound events are also created.
    val inductionScheduleEvents = inductionScheduleEventQueue.receiveEventsOnQueue(QueueType.INDUCTION)
    assertThat(inductionScheduleEvents).hasSize(2)
    assertThat(inductionScheduleEvents).allSatisfy {
      assertThat(it.personReference.identifiers[0].value).isEqualTo(prisonNumber)
      assertThat(it.detailUrl).isEqualTo("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
    }
  }
}
