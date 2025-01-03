package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.ADMISSION
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TRANSFERRED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RECEIVED_INTO_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.LocalDate
import java.time.OffsetDateTime
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule as InductionScheduleCalculationRuleResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus as InductionScheduleStatusResponse

@Isolated
class PrisonerReceivedEventTest : IntegrationTestBase() {

  @Nested
  inner class PrisonerAdmissions {
    @Test
    fun `should create new Induction Schedule and send outbound message given 'prisoner received' (admission) event for prisoner that does not already have an Induction`() {
      // Given
      val prisonNumber = "A6099EA"
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
      // wait until the queue is drained / message is processed
      await untilCallTo {
        domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
      } matches { it == 0 }

      // test induction schedule was created
      val inductionSchedule = getInductionSchedule(prisonNumber)
      assertThat(inductionSchedule)
        .wasCreatedAtOrAfter(earliestDateTime)
        .wasUpdatedAtOrAfter(earliestDateTime)
        .wasCreatedBy("system")
        .wasCreatedByDisplayName("system")
        .wasUpdatedBy("system")
        .wasUpdatedByDisplayName("system")
        .wasScheduleCalculationRule(InductionScheduleCalculationRuleResponse.NEW_PRISON_ADMISSION)
        .wasStatus(InductionScheduleStatusResponse.SCHEDULED)

      // test induction schedule history is created
      val inductionScheduleHistories = getInductionScheduleHistory("A6099EA")
      assertThat(inductionScheduleHistories.inductionSchedules).size().isEqualTo(1)
      assertThat(inductionScheduleHistories.inductionSchedules[0])
        .wasCreatedAtOrAfter(earliestDateTime)
        .wasUpdatedAtOrAfter(earliestDateTime)
        .wasCreatedBy("system")
        .wasCreatedByDisplayName("system")
        .wasUpdatedBy("system")
        .wasUpdatedByDisplayName("system")
        .wasScheduleCalculationRule(InductionScheduleCalculationRuleResponse.NEW_PRISON_ADMISSION)
        .wasStatus(InductionScheduleStatusResponse.SCHEDULED)
        .wasVersion(1)

      // test that outbound event is also created:
      val inductionScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.INDUCTION)
      assertThat(inductionScheduleEvent.personReference.identifiers[0].value).isEqualTo("A6099EA")
      assertThat(inductionScheduleEvent.detailUrl).isEqualTo("http://localhost:8080/inductions/A6099EA/induction-schedule")
    }

    @Test
    fun `should update the Review Schedule and send outbound message given 'prisoner received' (admission) event for prisoner that already has an Induction and initial Review Schedule`() {
      // Given
      // an induction schedule, induction and action plan are created. This will have created the initial Review Schedule
      val prisonNumber = randomValidPrisonNumber()
      createInductionSchedule(
        prisonNumber = prisonNumber,
        status = InductionScheduleStatus.COMPLETED,
      )
      createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork())
      createActionPlan(prisonNumber)

      // The above calls set the data up but they will also generate events so clear these out before starting the test.
      // Before clearing the queues though we need to wait until the first "plp.review-schedule.updated" event on the REVIEW queue is received.
      await untilCallTo {
        reviewScheduleEventQueue.countAllMessagesOnQueue()
      } matches { it != null && it > 0 }
      clearQueues()

      val expectedPrisoner = aValidPrisoner(prisonerNumber = prisonNumber)
      createPrisonerAPIStub(prisonNumber, expectedPrisoner)

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
      // wait until the queue is drained / message is processed
      await untilCallTo {
        domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
      } matches { it == 0 }

      val actionPlanReviews = getActionPlanReviews(prisonNumber)
      assertThat(actionPlanReviews)
        .latestReviewSchedule {
          it.hasStatus(ReviewScheduleStatus.SCHEDULED)
            // TODO fix the implementation in PefCiagKpiService.processPrisonerAdmission to make this set the correct calculation rule
            // .hasCalculationRule(ReviewScheduleCalculationRule.PRISONER_READMISSION)
            .wasCreatedAtOrAfter(earliestDateTime)
            .wasUpdatedAtOrAfter(earliestDateTime)
        }

      // test that outbound event is also created:
      val reviewScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.REVIEW)
      assertThat(reviewScheduleEvent.personReference.identifiers[0].value).isEqualTo(prisonNumber)
      assertThat(reviewScheduleEvent.detailUrl).isEqualTo("http://localhost:8080/reviews/$prisonNumber/review-schedule")
    }
  }

  @Nested
  inner class PrisonerTransfers {
    val prisonNumber = aValidPrisonNumber()
    val originalPrison = "BXI"
    val prisonTransferringTo = "MDI"

    @Test
    fun `should update Review Schedule and send outbound message given 'prisoner received' (transfer) event for prisoner that has an active Review Schedule with review date later than adjusted date`() {
      // Given
      // an induction and action plan are created. This will have created the initial Review Schedule with the status SCHEDULED
      createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork(prisonId = originalPrison))
      createActionPlan(prisonNumber)

      val expectedLatestReviewDate = LocalDate.now().plusDays(10)
      // Set the latest review date to be later than the adjusted date that a transfer might set
      updateReviewScheduleRecordLatestReviewDate(prisonNumber, expectedLatestReviewDate)

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
          prisonId = prisonTransferringTo,
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
            .wasUpdatedAtPrison(originalPrison)
        }
        .reviewScheduleAtVersion(3) {
          // Review Schedule version 3 is the re-scheduled review schedule
          it.hasStatus(ReviewScheduleStatus.SCHEDULED)
            .wasUpdatedAtPrison(prisonTransferringTo)
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
    fun `should update Review Schedule and send outbound message given 'prisoner received' (transfer) event for prisoner that has an active Review Schedule with review date earlier than adjusted date`() {
      // Given
      // an induction and action plan are created. This will have created the initial Review Schedule with the status SCHEDULED
      createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork(prisonId = originalPrison))
      createActionPlan(prisonNumber)

      val expectedLatestReviewDate = LocalDate.now().plusDays(5)
      // Set the latest review date to be earlier than the adjusted date that a transfer will set
      updateReviewScheduleRecordLatestReviewDate(prisonNumber, LocalDate.now().plusDays(4))

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
          prisonId = prisonTransferringTo,
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
            .wasUpdatedAtPrison(originalPrison)
        }
        .reviewScheduleAtVersion(3) {
          // Review Schedule version 3 is the re-scheduled review schedule
          it.hasStatus(ReviewScheduleStatus.SCHEDULED)
            .wasUpdatedAtPrison(prisonTransferringTo)
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
    fun `should not update Review Schedule and not send outbound message given 'prisoner received' (transfer) event for prisoner that does not have a Review Schedule at all`() {
      // Given
      assertThat(getReviewSchedules(prisonNumber)).hasNumberOfReviewSchedules(0)

      val sqsMessage = aValidHmppsDomainEventsSqsMessage(
        prisonNumber = prisonNumber,
        eventType = PRISONER_RECEIVED_INTO_PRISON,
        additionalInformation = aValidPrisonerReceivedAdditionalInformation(
          prisonNumber = prisonNumber,
          prisonId = prisonTransferringTo,
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

      assertThat(getReviewSchedules(prisonNumber)).hasNumberOfReviewSchedules(0)

      // test that no outbound events were created
      assertThat(reviewScheduleEventQueue.countAllMessagesOnQueue()).isEqualTo(0)
    }
  }
}
