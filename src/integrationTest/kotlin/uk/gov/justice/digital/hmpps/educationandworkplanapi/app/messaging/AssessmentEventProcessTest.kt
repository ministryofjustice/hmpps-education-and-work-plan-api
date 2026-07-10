package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilAsserted
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.events.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus as InductionScheduleStatusResponse

/**
 * Integration tests for the "all relevant S&As completed" message handling under the PES contract
 * (`ciag-kpi-processing-rule=PES`), which schedules a prisoner's pending Induction once their Screening &
 * Assessments are completed in Curious.
 */
@Isolated
class AssessmentEventProcessTest : IntegrationTestBase() {

  @Test
  fun `should schedule a pending induction when all relevant assessments completed`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()
    // A past completion date is sent to prove the deadline is calculated from today (today + 10), not the completion date
    val assessmentCompletionDate = LocalDate.now().minusDays(2)
    val inductionScheduleReference = UUID.randomUUID()

    createInductionSchedule(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS,
      deadlineDate = LocalDate.now(),
      createdAtPrison = "BXI",
    )
    createInductionScheduleHistory(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS,
      deadlineDate = LocalDate.now(),
      createdAtPrison = "BXI",
    )

    val sqsMessage = aValidSqsAssessmentEventMessage(
      messageAttributes = validMessageAttributes(
        prisonNumber = prisonNumber,
        statusChangeDate = assessmentCompletionDate,
      ),
    )

    // When
    sendAssessmentEvent(sqsMessage)

    // Then
    await untilCallTo {
      assessmentEventQueueClient.countMessagesOnQueue(assessmentEventQueue.queueUrl).get()
    } matches { it == 0 }

    // The pending Induction Schedule is scheduled with a deadline of today plus 10 days
    await untilAsserted {
      val inductionSchedule = getInductionSchedule(prisonNumber)
      assertThat(inductionSchedule)
        .wasStatus(InductionScheduleStatusResponse.SCHEDULED)
        .hasDeadlineDate(LocalDate.now().plusDays(10))
    }

    // test that outbound event is also created:
    val inductionScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.INDUCTION)
    assertThat(inductionScheduleEvent)
      .hasDetailUrl("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
      .hasNumberOfPersonReferenceIdentifiers(1)
      .personReferenceIdentifier(1) { it.hasValue(prisonNumber) }
  }

  @Test
  fun `should not change an induction schedule that is already scheduled when all relevant assessments completed`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()
    val existingDeadlineDate = LocalDate.now().plusDays(20)
    val inductionScheduleReference = UUID.randomUUID()

    createInductionSchedule(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.SCHEDULED,
      deadlineDate = existingDeadlineDate,
      createdAtPrison = "BXI",
    )

    val sqsMessage = aValidSqsAssessmentEventMessage(
      messageAttributes = validMessageAttributes(prisonNumber = prisonNumber),
    )

    // When
    sendAssessmentEvent(sqsMessage)

    // Then
    await untilCallTo {
      assessmentEventQueueClient.countMessagesOnQueue(assessmentEventQueue.queueUrl).get()
    } matches { it == 0 }

    // The assessment event is recorded, but the already-scheduled Induction is left unchanged
    await untilAsserted {
      val events = educationAssessmentEventRepository.findByPrisonNumber(prisonNumber)
      assert(events.size == 1)
    }

    val inductionSchedule = getInductionSchedule(prisonNumber)
    assertThat(inductionSchedule)
      .wasStatus(InductionScheduleStatusResponse.SCHEDULED)
      .hasDeadlineDate(existingDeadlineDate)

    // assert no Induction or Review messages were sent to MN
    assertThat(inductionScheduleEventQueue.countAllMessagesOnQueue()).isEqualTo(0)
    assertThat(reviewScheduleEventQueue.countAllMessagesOnQueue()).isEqualTo(0)
  }

  @Test
  fun `should not change an induction schedule that is already completed when all relevant assessments completed`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()
    val existingDeadlineDate = LocalDate.now().plusDays(20)
    val inductionScheduleReference = UUID.randomUUID()

    createInductionSchedule(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.COMPLETED,
      deadlineDate = existingDeadlineDate,
      createdAtPrison = "BXI",
    )

    val sqsMessage = aValidSqsAssessmentEventMessage(
      messageAttributes = validMessageAttributes(prisonNumber = prisonNumber),
    )

    // When
    sendAssessmentEvent(sqsMessage)

    // Then
    await untilCallTo {
      assessmentEventQueueClient.countMessagesOnQueue(assessmentEventQueue.queueUrl).get()
    } matches { it == 0 }

    // The assessment event is recorded, but the already-scheduled Induction is left unchanged
    await untilAsserted {
      val events = educationAssessmentEventRepository.findByPrisonNumber(prisonNumber)
      assert(events.size == 1)
    }

    val inductionSchedule = getInductionSchedule(prisonNumber)
    assertThat(inductionSchedule)
      .wasStatus(InductionScheduleStatusResponse.COMPLETED)
      .hasDeadlineDate(existingDeadlineDate)

    // assert no Induction or Review messages were sent to MN
    assertThat(inductionScheduleEventQueue.countAllMessagesOnQueue()).isEqualTo(0)
    assertThat(reviewScheduleEventQueue.countAllMessagesOnQueue()).isEqualTo(0)
  }

  @Test
  fun `should not change an induction schedule that is already exempt when all relevant assessments completed`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()
    val existingDeadlineDate = LocalDate.now().plusDays(20)
    val inductionScheduleReference = UUID.randomUUID()

    createInductionSchedule(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.EXEMPT_PRISONER_SAFETY_ISSUES,
      deadlineDate = existingDeadlineDate,
      createdAtPrison = "BXI",
    )

    val sqsMessage = aValidSqsAssessmentEventMessage(
      messageAttributes = validMessageAttributes(prisonNumber = prisonNumber),
    )

    // When
    sendAssessmentEvent(sqsMessage)

    // Then
    await untilCallTo {
      assessmentEventQueueClient.countMessagesOnQueue(assessmentEventQueue.queueUrl).get()
    } matches { it == 0 }

    // The assessment event is recorded, but the already-scheduled Induction is left unchanged
    await untilAsserted {
      val events = educationAssessmentEventRepository.findByPrisonNumber(prisonNumber)
      assert(events.size == 1)
    }

    val inductionSchedule = getInductionSchedule(prisonNumber)
    assertThat(inductionSchedule)
      .wasStatus(InductionScheduleStatusResponse.EXEMPT_PRISONER_SAFETY_ISSUES)
      .hasDeadlineDate(existingDeadlineDate)

    // assert no Induction or Review messages were sent to MN
    assertThat(inductionScheduleEventQueue.countAllMessagesOnQueue()).isEqualTo(0)
    assertThat(reviewScheduleEventQueue.countAllMessagesOnQueue()).isEqualTo(0)
  }

  @Test
  fun `should record assessment event but not create an induction schedule given prisoner has no induction schedule`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()
    // The prisoner has no Induction Schedule (messages out of sequence - the S&A Complete arrived before any admission)

    val sqsMessage = aValidSqsAssessmentEventMessage(
      messageAttributes = validMessageAttributes(prisonNumber = prisonNumber),
    )

    // When
    sendAssessmentEvent(sqsMessage)

    // Then
    await untilCallTo {
      assessmentEventQueueClient.countMessagesOnQueue(assessmentEventQueue.queueUrl).get()
    } matches { it == 0 }

    // The assessment event is recorded, but no Induction Schedule is created
    await untilAsserted {
      val events = educationAssessmentEventRepository.findByPrisonNumber(prisonNumber)
      assert(events.size == 1)
    }
    assertThat(getInductionScheduleHistory(prisonNumber)).hasNumberOfInductionScheduleVersions(0)
  }

  @Test
  fun `should be idempotent on the induction schedule given a duplicate assessments completed message`() {
    // Given a prisoner with a pending Induction Schedule (seeded as version 1)
    val prisonNumber = setUpRandomPrisoner()
    val inductionScheduleReference = UUID.randomUUID()

    createInductionSchedule(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS,
      deadlineDate = LocalDate.now(),
      createdAtPrison = "BXI",
    )
    createInductionScheduleHistory(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS,
      deadlineDate = LocalDate.now(),
      createdAtPrison = "BXI",
    )

    val firstMessage = aValidSqsAssessmentEventMessage(
      messageAttributes = validMessageAttributes(
        prisonNumber = prisonNumber,
        statusChangeDate = LocalDate.of(2026, 3, 10),
      ),
    )
    val secondMessage = aValidSqsAssessmentEventMessage(
      messageAttributes = validMessageAttributes(
        prisonNumber = prisonNumber,
        statusChangeDate = LocalDate.of(2026, 3, 15),
      ),
    )

    // When the assessments-completed event is processed twice
    sendAssessmentEvent(firstMessage)
    await untilCallTo {
      assessmentEventQueueClient.countMessagesOnQueue(assessmentEventQueue.queueUrl).get()
    } matches { it == 0 }
    sendAssessmentEvent(secondMessage)
    await untilCallTo {
      assessmentEventQueueClient.countMessagesOnQueue(assessmentEventQueue.queueUrl).get()
    } matches { it == 0 }

    // Then the Induction Schedule is scheduled exactly once (today + 10); the second message is a no-op as it is no
    // longer pending, so no further history version is created (version 1 = seeded PENDING, version 2 = SCHEDULED)
    await untilAsserted {
      assertThat(getInductionScheduleHistory(prisonNumber)).hasNumberOfInductionScheduleVersions(2)
    }
    assertThat(getInductionSchedule(prisonNumber))
      .wasStatus(InductionScheduleStatusResponse.SCHEDULED)
      .hasDeadlineDate(LocalDate.now().plusDays(10))
  }
}
