package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.ADMISSION
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RECEIVED_INTO_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.LocalDate
import java.time.OffsetDateTime
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule as InductionScheduleCalculationRuleResponse

@Isolated
class PrisonerReceivedDueToAdmissionEventTest : IntegrationTestBase() {

  companion object {
    private val TODAY = LocalDate.now()
  }

  @Test
  fun `should create new Induction Schedule and send outbound message given 'prisoner received' (admission) event for prisoner that does not already have an Induction`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
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
      .wasStatus(SCHEDULED)

    // test induction schedule history is created
    val inductionScheduleHistories = getInductionScheduleHistory(prisonNumber)
    assertThat(inductionScheduleHistories.inductionSchedules).size().isEqualTo(1)
    assertThat(inductionScheduleHistories.inductionSchedules[0])
      .wasCreatedAtOrAfter(earliestDateTime)
      .wasUpdatedAtOrAfter(earliestDateTime)
      .wasCreatedBy("system")
      .wasCreatedByDisplayName("system")
      .wasUpdatedBy("system")
      .wasUpdatedByDisplayName("system")
      .wasScheduleCalculationRule(InductionScheduleCalculationRuleResponse.NEW_PRISON_ADMISSION)
      .wasStatus(SCHEDULED)
      .wasVersion(1)

    // test that outbound event is also created:
    val inductionScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.INDUCTION)
    assertThat(inductionScheduleEvent.personReference.identifiers[0].value).isEqualTo(prisonNumber)
    assertThat(inductionScheduleEvent.detailUrl).isEqualTo("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
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
          .hasCalculationRule(ReviewScheduleCalculationRule.PRISONER_READMISSION)
          .wasCreatedAtOrAfter(earliestDateTime)
          .wasUpdatedAtOrAfter(earliestDateTime)
      }

    // test that outbound event is also created:
    val reviewScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.REVIEW)
    assertThat(reviewScheduleEvent.personReference.identifiers[0].value).isEqualTo(prisonNumber)
    assertThat(reviewScheduleEvent.detailUrl).isEqualTo("http://localhost:8080/reviews/$prisonNumber/review-schedule")
  }

  @Test
  fun `should re-schedule the Induction Schedule and send outbound message given 'prisoner received' (admission) event for prisoner that already has an incomplete Induction Schedule`() {
    // Given
    // an induction schedule is created
    val prisonNumber = randomValidPrisonNumber()
    val originalInductionDueDate = TODAY.minusWeeks(10)
    createInductionSchedule(
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.SCHEDULED,
      deadlineDate = originalInductionDueDate,
    )

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RECEIVED_INTO_PRISON,
      additionalInformation = aValidPrisonerReceivedAdditionalInformation(
        prisonNumber = prisonNumber,
        reason = ADMISSION,
      ),
    )

    val expectedInductionDueDate = TODAY.plusDays(20) // based on PEF rules as currently set in  application-integration-test.yml

    // When
    sendDomainEvent(sqsMessage)

    // Then
    // wait until the queue is drained / message is processed
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    val inductionSchedule = getInductionSchedule(prisonNumber)
    assertThat(inductionSchedule)
      .wasStatus(SCHEDULED)
      .hasDeadlineDate(expectedInductionDueDate)
      .wasCreatedBy("system")
      .wasUpdatedBy("system")

    // test that outbound event is also created:
    val reviewScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.INDUCTION)
    assertThat(reviewScheduleEvent.personReference.identifiers[0].value).isEqualTo(prisonNumber)
    assertThat(reviewScheduleEvent.detailUrl).isEqualTo("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
  }

  @Test
  fun `should not re-schedule the Induction Schedule and send outbound message given 'prisoner received' (admission) event for prisoner that already has a PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS Induction Schedule`() {
    // Given
    // an induction schedule is created
    val prisonNumber = randomValidPrisonNumber()
    val originalInductionDueDate = TODAY.minusWeeks(10)
    createInductionSchedule(
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS,
      deadlineDate = originalInductionDueDate,
    )

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

    val inductionSchedule = getInductionSchedule(prisonNumber)
    assertThat(inductionSchedule)
      .wasStatus(PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS)
      .hasDeadlineDate(originalInductionDueDate)

    // test that no outbound events were created
    assertThat(inductionScheduleEventQueue.countAllMessagesOnQueue()).isEqualTo(0)
  }
}
