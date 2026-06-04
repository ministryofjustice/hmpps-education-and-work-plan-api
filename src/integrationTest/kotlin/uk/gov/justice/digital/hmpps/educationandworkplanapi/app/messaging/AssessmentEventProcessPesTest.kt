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
}
