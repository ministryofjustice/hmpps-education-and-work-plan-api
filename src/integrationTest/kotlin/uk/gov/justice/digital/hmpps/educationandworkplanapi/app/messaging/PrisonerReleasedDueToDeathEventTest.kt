package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilAsserted
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RELEASED_FROM_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus as InductionScheduleStatusEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus as ReviewScheduleStatusEntity

@Isolated
class PrisonerReleasedDueToDeathEventTest : IntegrationTestBase() {
  @Test
  fun `should update Review Schedule given deceased prisoner had an active Review Schedule`() {
    // Given
    // an induction and action plan are created. This will have created the initial Review Schedule with the status SCHEDULED
    val prisonNumber = setUpRandomPrisoner()
    createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork())
    createActionPlan(prisonNumber)

    // The above calls set the data up but they will also generate events so clear these out before starting the test.
    // Before clearing the queues though we need to wait until the "plp.review-schedule.updated" event on the REVIEW queue is received.
    await untilCallTo {
      reviewScheduleEventQueue.countAllMessagesOnQueue()
    } matches { it != null && it > 0 }
    clearQueues()

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RELEASED_FROM_PRISON,
      additionalInformation = aValidPrisonerReleasedAdditionalInformation(
        prisonNumber = prisonNumber,
        nomisMovementReasonCode = "DEC",
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
        it.hasStatus(ReviewScheduleStatus.EXEMPT_PRISONER_DEATH)
      }
  }

  @Test
  fun `should not update Review Schedule given released prisoner does not have an active Review Schedule`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()
    // an induction and action plan are created. This will have created the initial Review Schedule with the status SCHEDULED
    createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork())
    createActionPlan(prisonNumber)
    // Before updating the status of the Review Schedule we need to wait until 1 "plp.review-schedule.updated" event on the REVIEW queue is received (which is the creation of the initial Review Schedule)
    await untilCallTo {
      reviewScheduleEventQueue.countAllMessagesOnQueue()
    } matches { it != null && it > 0 }
    // Update the status of the Review Schedule to Completed, which means the prisoner no longer has an active Review Schedule
    updateReviewScheduleRecordStatus(prisonNumber, ReviewScheduleStatusEntity.COMPLETED)

    clearQueues()

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RELEASED_FROM_PRISON,
      additionalInformation = aValidPrisonerReleasedAdditionalInformation(
        prisonNumber = prisonNumber,
        nomisMovementReasonCode = "DEC",
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
        it.hasStatus(ReviewScheduleStatus.COMPLETED)
      }
  }

  @Test
  fun `should not create or update Review Schedule given released prisoner does not have a Review Schedule at all`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()
    assertThat(runCatching { getActionPlanReviews(prisonNumber) }.getOrNull()).isNull()

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RELEASED_FROM_PRISON,
      additionalInformation = aValidPrisonerReleasedAdditionalInformation(
        prisonNumber = prisonNumber,
        nomisMovementReasonCode = "DEC",
      ),
    )

    // When
    sendDomainEvent(sqsMessage)

    // Then
    // wait until the queue is drained / message is processed
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    assertThat(runCatching { getActionPlanReviews(prisonNumber) }.getOrNull()).isNull()
  }

  @Test
  fun `should update Induction Schedule given deceased prisoner had an active Induction Schedule`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()
    createInductionSchedule(prisonNumber)

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RELEASED_FROM_PRISON,
      additionalInformation = aValidPrisonerReleasedAdditionalInformation(
        prisonNumber = prisonNumber,
        nomisMovementReasonCode = "DEC",
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
      val inductionSchedule = getInductionSchedule(prisonNumber)
      assertThat(inductionSchedule)
        .wasStatus(InductionScheduleStatus.EXEMPT_PRISONER_DEATH)
    }
  }

  @Test
  fun `should not update Induction Schedule given deceased prisoner had a completed Induction Schedule`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()
    createInductionSchedule(prisonNumber, status = InductionScheduleStatusEntity.COMPLETED)

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RELEASED_FROM_PRISON,
      additionalInformation = aValidPrisonerReleasedAdditionalInformation(
        prisonNumber = prisonNumber,
        nomisMovementReasonCode = "DEC",
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
      val inductionSchedule = getInductionSchedule(prisonNumber)
      assertThat(inductionSchedule)
        .wasStatus(InductionScheduleStatus.COMPLETED)
    }
  }
}
