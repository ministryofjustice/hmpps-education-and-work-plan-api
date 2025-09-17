package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TEMPORARY_ABSENCE_RETURN
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RECEIVED_INTO_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.LocalDate

@Isolated
class PrisonerReceivedEventDueToTempAbsenceTest : IntegrationTestBase() {

  @Test
  fun `should update Review Schedule and send outbound message given 'prisoner received' (TEMP_ABSENCE_RETURN) event for prisoner that has an active Review Schedule`() {
    // Given
    // an induction and action plan are created. This will have created the initial Review Schedule with the status SCHEDULED
    val prisonNumber = setUpRandomPrisoner()
    createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork())
    createActionPlan(prisonNumber)

    // Set the review dates so that we can assert that they've been changed
    updateReviewScheduleReviewDates(
      prisonNumber = prisonNumber,
      earliestReviewDate = LocalDate.now(),
      latestReviewDate = LocalDate.now().plusDays(1),
    )

    val expectedEarliestReviewDate = LocalDate.now()
    val expectedLatestReviewDate = LocalDate.now().plusDays(5)

    // The above calls set the data up, but they will also generate events so clear these out before starting the test.
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

    val reviewSchedules = getReviewSchedules(prisonNumber)
    assertThat(reviewSchedules)
      // Expect 3 schedules - 1 is the initial scheduled, 2 is exemption due to TAP, 3 is the re-scheduled
      .hasNumberOfReviewSchedules(3)
      .reviewScheduleAtVersion(2) {
        // Review Schedule version 2 is the exempted schedule due to transfer
        it.hasStatus(ReviewScheduleStatus.EXEMPT_TEMP_ABSENCE)
      }
      .reviewScheduleAtVersion(3) {
        // Review Schedule version 3 is the re-scheduled review schedule
        it.hasStatus(ReviewScheduleStatus.SCHEDULED)
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
  fun `should not update Review Schedule and not send outbound message given 'prisoner received' (TEMP_ABSENCE_RETURN) event for prisoner that does not have a Review Schedule at all`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()
    assertThat(getReviewSchedules(prisonNumber)).hasNumberOfReviewSchedules(0)

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

    assertThat(getReviewSchedules(prisonNumber)).hasNumberOfReviewSchedules(0)

    // test that no outbound events were created
    assertThat(reviewScheduleEventQueue.countAllMessagesOnQueue()).isEqualTo(0)
  }
}
