package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilAsserted
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus.COMPLETED
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
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule as InductionScheduleCalculationRuleResponse

@Isolated
class PrisonerReceivedDueToAdmissionEventTest : IntegrationTestBase() {

  companion object {
    private val TODAY = LocalDate.now()
  }

  // New prison admission, prisoner has never had a PLP
  @Test
  fun `should create new Induction Schedule given prisoner does not already have an Induction`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    with(aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "MDI")) {
      createPrisonerAPIStub(prisonNumber, this)
    }

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

    await untilAsserted {
      // test induction schedule was created
      val inductionSchedule = getInductionSchedule(prisonNumber)
      assertThat(inductionSchedule)
        .wasCreatedAtOrAfter(earliestDateTime)
        .wasUpdatedAtOrAfter(earliestDateTime)
        .wasCreatedBy("system")
        .wasCreatedByDisplayName("system")
        .wasCreatedAtPrison("MDI")
        .wasUpdatedBy("system")
        .wasUpdatedByDisplayName("system")
        .wasUpdatedAtPrison("MDI")
        .wasScheduleCalculationRule(InductionScheduleCalculationRuleResponse.NEW_PRISON_ADMISSION)
        .wasStatus(SCHEDULED)

      // test induction schedule history is created
      val inductionScheduleHistories = getInductionScheduleHistory(prisonNumber)
      assertThat(inductionScheduleHistories)
        .hasNumberOfInductionScheduleVersions(1)
        .inductionScheduleVersion(1) {
          it.wasCreatedAtOrAfter(earliestDateTime)
            .wasUpdatedAtOrAfter(earliestDateTime)
            .wasCreatedBy("system")
            .wasCreatedByDisplayName("system")
            .wasCreatedAtPrison("MDI")
            .wasUpdatedBy("system")
            .wasUpdatedByDisplayName("system")
            .wasUpdatedAtPrison("MDI")
            .wasScheduleCalculationRule(InductionScheduleCalculationRuleResponse.NEW_PRISON_ADMISSION)
            .wasStatus(SCHEDULED)
            .wasVersion(1)
        }

      // test that outbound event is also created:
      val inductionScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.INDUCTION)
      assertThat(inductionScheduleEvent.personReference.identifiers[0].value).isEqualTo(prisonNumber)
      assertThat(inductionScheduleEvent.detailUrl).isEqualTo("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
    }
  }

  // Re-offending prisoner, re-admitted to prison. Prisoner previously had a PLP Induction that was completed and a completed Review Schedule
  @Test
  fun `should create a new Review Schedule given prisoner that already has a completed Induction and completed Review Schedule`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    with(aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "BXI")) {
      createPrisonerAPIStub(prisonNumber, this)
    }

    // Create the induction schedule, induction and action plan. This will have created the initial Review Schedule
    createInductionSchedule(
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.COMPLETED,
      createdAtPrison = "BXI",
    )
    createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork(prisonId = "BXI"))
    createActionPlan(prisonNumber)
    updateReviewScheduleRecordStatus(
      prisonNumber = prisonNumber,
      status = COMPLETED,
    )

    // The above calls set the data up but they will also generate events so clear these out before starting the test.
    // Before clearing the queues though we need to wait until the first "plp.review-schedule.updated" event on the REVIEW queue is received.
    await untilCallTo {
      reviewScheduleEventQueue.countAllMessagesOnQueue()
    } matches { it != null && it > 0 }
    clearQueues()

    with(aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "MDI")) {
      createPrisonerAPIStub(prisonNumber, this)
    }

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

    await untilAsserted {
      val actionPlanReviews = getActionPlanReviews(prisonNumber)
      assertThat(actionPlanReviews)
        .latestReviewSchedule {
          it.hasStatus(ReviewScheduleStatus.SCHEDULED)
            .hasCalculationRule(ReviewScheduleCalculationRule.PRISONER_READMISSION)
            .wasCreatedAtOrAfter(earliestDateTime)
            .wasUpdatedAtOrAfter(earliestDateTime)
            .wasCreatedAtPrison("MDI")
            .wasUpdatedAtPrison("MDI")
        }

      // test that outbound event is also created:
      val reviewScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.REVIEW)
      assertThat(reviewScheduleEvent.personReference.identifiers[0].value).isEqualTo(prisonNumber)
      assertThat(reviewScheduleEvent.detailUrl).isEqualTo("http://localhost:8080/reviews/$prisonNumber/review-schedule")
    }
  }

  // Re-offending prisoner, re-admitted to prison. Prisoner previously had a PLP Induction Schedule but it was never completed
  @Test
  fun `should re-schedule the Induction Schedule given prisoner that already has an incomplete Induction Schedule`() {
    // Given
    // an induction schedule is created
    val prisonNumber = randomValidPrisonNumber()
    val originalInductionDueDate = TODAY.minusWeeks(10)
    val inductionScheduleReference = UUID.randomUUID()
    createInductionSchedule(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.SCHEDULED,
      deadlineDate = originalInductionDueDate,
      createdAtPrison = "BXI",
    )
    createInductionScheduleHistory(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.SCHEDULED,
      deadlineDate = originalInductionDueDate,
      createdAtPrison = "BXI",
    )

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RECEIVED_INTO_PRISON,
      additionalInformation = aValidPrisonerReceivedAdditionalInformation(
        prisonNumber = prisonNumber,
        reason = ADMISSION,
      ),
    )

    with(aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "MDI")) {
      createPrisonerAPIStub(prisonNumber, this)
    }

    val expectedInductionDueDate = TODAY.plusDays(20) // based on PEF rules as currently set in  application-integration-test.yml

    // When
    sendDomainEvent(sqsMessage)

    // Then
    // wait until the queue is drained / message is processed
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    await untilAsserted {
      val inductionSchedules = getInductionScheduleHistory(prisonNumber)
      assertThat(inductionSchedules)
        .hasNumberOfInductionScheduleVersions(2)
        .inductionScheduleVersion(1) {
          it.wasStatus(SCHEDULED)
            .hasDeadlineDate(originalInductionDueDate)
            .wasCreatedAtPrison("BXI")
            .wasUpdatedAtPrison("BXI")
        }
        .inductionScheduleVersion(2) {
          it.wasStatus(SCHEDULED)
            .hasDeadlineDate(expectedInductionDueDate)
            .wasCreatedAtPrison("BXI")
            .wasUpdatedAtPrison("MDI")
        }

      // test that outbound event is also created:
      val reviewScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.INDUCTION)
      assertThat(reviewScheduleEvent.personReference.identifiers[0].value).isEqualTo(prisonNumber)
      assertThat(reviewScheduleEvent.detailUrl).isEqualTo("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
    }
  }

  // Re-offending prisoner, re-admitted to prison. Prisoner previously had a PLP Induction Schedule but it was never scheduled because it was still waiting for Curious S&As
  @Test
  fun `should not re-schedule the Induction Schedule given prisoner that already has a PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS Induction Schedule`() {
    // Given
    // an induction schedule is created
    val prisonNumber = randomValidPrisonNumber()
    val originalInductionDueDate = TODAY.minusWeeks(10)
    val inductionScheduleReference = UUID.randomUUID()
    createInductionSchedule(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS,
      deadlineDate = originalInductionDueDate,
    )
    createInductionScheduleHistory(
      reference = inductionScheduleReference,
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

    await untilAsserted {
      val inductionSchedules = getInductionScheduleHistory(prisonNumber)
      assertThat(inductionSchedules)
        .hasNumberOfInductionScheduleVersions(1)
        .inductionScheduleVersion(1) {
          it.wasStatus(PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS)
            .hasDeadlineDate(originalInductionDueDate)
            .wasVersion(1)
        }

      // test that no outbound events were created
      assertThat(inductionScheduleEventQueue.countAllMessagesOnQueue()).isEqualTo(0)
    }
  }
}
