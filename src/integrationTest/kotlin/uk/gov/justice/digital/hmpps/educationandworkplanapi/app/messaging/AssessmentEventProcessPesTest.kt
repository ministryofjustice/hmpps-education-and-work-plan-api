package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilAsserted
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus
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
@TestPropertySource(properties = ["ciag-kpi-processing-rule=PES"])
class AssessmentEventProcessPesTest : IntegrationTestBase() {

  @BeforeEach
  fun setUp() {
    educationAssessmentEventRepository.deleteAll()
  }

  @Test
  fun `should schedule a pending induction when all relevant assessments completed`() {
    // Given
    val prisonNumber = "G0378GI"
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

    wiremockService.stubGetPrisonerFromPrisonerSearchApi(
      prisonNumber,
      aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "BXI"),
    )

    val sqsMessage = SqsAssessmentEventMessage(
      messageId = "14e2865f-1e4b-43b0-87e8-874e7e238dd9",
      eventType = "EducationAssessmentEventCreated",
      messageAttributes = MessageAttributes(
        prisonNumber = prisonNumber,
        status = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = assessmentCompletionDate,
        detailUrl = "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/$prisonNumber",
        requestId = "0650ba37-a977-4fbe-9000-4715aaecadba",
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
  }

  @Test
  fun `should not change an induction that is already scheduled when all relevant assessments completed`() {
    // Given
    val prisonNumber = "G0379GI"
    val existingDeadlineDate = LocalDate.now().plusDays(20)
    val inductionScheduleReference = UUID.randomUUID()

    createInductionSchedule(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.SCHEDULED,
      deadlineDate = existingDeadlineDate,
      createdAtPrison = "BXI",
    )

    wiremockService.stubGetPrisonerFromPrisonerSearchApi(
      prisonNumber,
      aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "BXI"),
    )

    val sqsMessage = SqsAssessmentEventMessage(
      messageId = "24e2865f-1e4b-43b0-87e8-874e7e238dd9",
      eventType = "EducationAssessmentEventCreated",
      messageAttributes = MessageAttributes(
        prisonNumber = prisonNumber,
        status = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.now(),
        detailUrl = "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/$prisonNumber",
        requestId = "1650ba37-a977-4fbe-9000-4715aaecadba",
      ),
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
  }

  @Test
  fun `should record assessment event but not create an induction schedule given prisoner has no induction schedule`() {
    // Given
    val prisonNumber = "G0380GI"
    // The prisoner has no Induction Schedule (messages out of sequence - the S&A Complete arrived before any admission)

    wiremockService.stubGetPrisonerFromPrisonerSearchApi(
      prisonNumber,
      aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "BXI"),
    )

    val sqsMessage = SqsAssessmentEventMessage(
      messageId = "34e2865f-1e4b-43b0-87e8-874e7e238dd9",
      eventType = "EducationAssessmentEventCreated",
      messageAttributes = MessageAttributes(
        prisonNumber = prisonNumber,
        status = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.now(),
        detailUrl = "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/$prisonNumber",
        requestId = "2650ba37-a977-4fbe-9000-4715aaecadba",
      ),
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
    val prisonNumber = "G0381GI"
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

    wiremockService.stubGetPrisonerFromPrisonerSearchApi(
      prisonNumber,
      aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "BXI"),
    )

    fun assessmentMessage(messageId: String, requestId: String) = SqsAssessmentEventMessage(
      messageId = messageId,
      eventType = "EducationAssessmentEventCreated",
      messageAttributes = MessageAttributes(
        prisonNumber = prisonNumber,
        status = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.now(),
        detailUrl = "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/$prisonNumber",
        requestId = requestId,
      ),
    )

    // When the assessments-completed event is processed twice
    sendAssessmentEvent(assessmentMessage("44e2865f-1e4b-43b0-87e8-874e7e238dd9", "3650ba37-a977-4fbe-9000-4715aaecadba"))
    await untilCallTo {
      assessmentEventQueueClient.countMessagesOnQueue(assessmentEventQueue.queueUrl).get()
    } matches { it == 0 }
    sendAssessmentEvent(assessmentMessage("55f3976g-2f5c-54c1-98f9-985f6bbfdbea", "4650ba37-a977-4fbe-9000-4715aaecadba"))
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
