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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RECEIVED_INTO_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.OffsetDateTime
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule as InductionScheduleCalculationRuleResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus as InductionScheduleStatusResponse

@Isolated
class PrisonerReceivedEventTest : IntegrationTestBase() {
  @Test
  fun `should create new Induction Schedule and send outbound message given 'prisoner received' event for prisoner that does not already have an Induction`() {
    // Given
    val prisonNumber = "A6099EA"
    assertThat(runCatching { getInduction(prisonNumber) }.getOrNull()).isNull()
    assertThat(runCatching { getInductionSchedule(prisonNumber) }.getOrNull()).isNull()

    val earliestDateTime = OffsetDateTime.now()

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RECEIVED_INTO_PRISON,
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
  fun `should update the Review Schedule and send outbound message given 'prisoner received' event for prisoner that already has an Induction and initial Review Schedule`() {
    // Given
    // an induction schedule, induction and action plan are created. This will have created the initial Review Schedule
    val prisonNumber = randomValidPrisonNumber()
    createInductionSchedule(
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.COMPLETE,
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
