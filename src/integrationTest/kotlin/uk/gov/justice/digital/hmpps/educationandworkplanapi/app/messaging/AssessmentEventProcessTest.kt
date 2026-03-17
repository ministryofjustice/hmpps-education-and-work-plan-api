package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilAsserted
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.LocalDate

@Isolated
class AssessmentEventProcessTest : IntegrationTestBase() {

  @BeforeEach
  fun setUp() {
    educationAssessmentEventRepository.deleteAll()
  }

  @Test
  fun `should process assessment event message and persist record`() {
    // Given
    val prisonNumber = "G0378GI"
    val statusChangeDate = LocalDate.now()

    wiremockService.stubGetPrisonerFromPrisonerSearchApi(
      prisonNumber,
      aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "BXI"),
    )

    val sqsMessage = SqsAssessmentEventMessage(
      messageId = "14e2865f-1e4b-43b0-87e8-874e7e238dd9",
      eventType = "EducationAssessmentEventCreated",
      description = null,
      who = null,
      messageAttributes = MessageAttributes(
        prisonNumber = prisonNumber,
        status = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = statusChangeDate,
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

    await untilAsserted {
      val events = educationAssessmentEventRepository.findByPrisonNumber(prisonNumber)
      assertThat(events).hasSize(1)
      with(events[0]) {
        assertThat(prisonNumber).isEqualTo("G0378GI")
        assertThat(status).isEqualTo(EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE)
        assertThat(statusChangeDate).isEqualTo(statusChangeDate)
        assertThat(source).isEqualTo("CURIOUS")
        assertThat(detailUrl).isEqualTo("https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/$prisonNumber")
        assertThat(createdAtPrison).isEqualTo("BXI")
        assertThat(updatedAtPrison).isEqualTo("BXI")
      }
    }
  }

  @Test
  fun `should persist multiple assessment events for the same prisoner`() {
    // Given
    val prisonNumber = "G0378GI"

    wiremockService.stubGetPrisonerFromPrisonerSearchApi(
      prisonNumber,
      aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "BXI"),
    )

    val firstMessage = SqsAssessmentEventMessage(
      messageId = "14e2865f-1e4b-43b0-87e8-874e7e238dd9",
      eventType = "EducationAssessmentEventCreated",
      messageAttributes = MessageAttributes(
        prisonNumber = prisonNumber,
        status = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.of(2026, 3, 10),
        detailUrl = "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/$prisonNumber",
        requestId = "0650ba37-a977-4fbe-9000-4715aaecadba",
      ),
    )

    val secondMessage = SqsAssessmentEventMessage(
      messageId = "25f3976g-2f5c-54c1-98f9-985f6bbfdbea",
      eventType = "EducationAssessmentEventCreated",
      messageAttributes = MessageAttributes(
        prisonNumber = prisonNumber,
        status = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.of(2026, 3, 15),
        detailUrl = "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/$prisonNumber",
        requestId = "1761cb48-b088-5fce-a111-5826bbfdcebb",
      ),
    )

    // When
    sendAssessmentEvent(firstMessage)
    await untilCallTo {
      assessmentEventQueueClient.countMessagesOnQueue(assessmentEventQueue.queueUrl).get()
    } matches { it == 0 }

    sendAssessmentEvent(secondMessage)
    await untilCallTo {
      assessmentEventQueueClient.countMessagesOnQueue(assessmentEventQueue.queueUrl).get()
    } matches { it == 0 }

    // Then
    await untilAsserted {
      val events = educationAssessmentEventRepository.findByPrisonNumber(prisonNumber)
      assertThat(events).hasSize(2)
      assertThat(events.map { it.statusChangeDate }).containsExactlyInAnyOrder(
        LocalDate.of(2026, 3, 10),
        LocalDate.of(2026, 3, 15),
      )
    }
  }
}
