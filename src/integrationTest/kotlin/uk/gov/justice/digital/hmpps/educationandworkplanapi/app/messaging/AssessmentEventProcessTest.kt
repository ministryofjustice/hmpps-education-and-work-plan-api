package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilAsserted
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.timeline.assertThat
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

    // Verify record persisted in database
    await untilAsserted {
      val events = educationAssessmentEventRepository.findByPrisonNumber(prisonNumber)
      assertThat(events)
        .hasNumberOfEvents(1)
        .event(1) {
          it.hasPrisonNumber("G0378GI")
            .hasStatus(EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE)
            .hasStatusChangeDate(statusChangeDate)
            .hasSource("CURIOUS")
            .hasDetailUrl("https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/$prisonNumber")
            .wasCreatedAtPrison("BXI")
            .wasUpdatedAtPrison("BXI")
        }
    }

    // Verify timeline event created
    await untilAsserted {
      val timeline = getTimeline(prisonNumber)
      assertThat(timeline).anyEvent { it.hasEventType(TimelineEventType.EDUCATION_ASSESSMENT_EVENT_CREATED) }
    }

    // Verify App Insights telemetry event sent
    await untilAsserted {
      val eventPropertiesCaptor = createCaptor<Map<String, String>>()
      verify(telemetryClient).trackEvent(
        eq("EDUCATION_ASSESSMENT_EVENT_CREATED"),
        capture(eventPropertiesCaptor),
        isNull(),
      )
      val eventProperties = eventPropertiesCaptor.value
      assertThat(eventProperties)
        .containsEntry("prisonNumber", prisonNumber)
        .containsEntry("status", "ALL_RELEVANT_ASSESSMENTS_COMPLETE")
        .containsEntry("source", "CURIOUS")
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
      assertThat(events)
        .hasNumberOfEvents(2)
        .hasStatusChangeDatesInAnyOrder(
          LocalDate.of(2026, 3, 10),
          LocalDate.of(2026, 3, 15),
        )
    }
  }

  @Test
  fun `should process assessment event message and persist record given prisoner retrieval fails`() {
    // Given
    val prisonNumber = "G0378GI"
    val statusChangeDate = LocalDate.now()

    wiremockService.stubGetPrisonerNotFound(prisonNumber)

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

    // Verify record persisted in database
    await untilAsserted {
      val events = educationAssessmentEventRepository.findByPrisonNumber(prisonNumber)
      assertThat(events)
        .hasNumberOfEvents(1)
        .event(1) {
          it.hasPrisonNumber("G0378GI")
            .hasStatus(EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE)
            .hasStatusChangeDate(statusChangeDate)
            .hasSource("CURIOUS")
            .hasDetailUrl("https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/$prisonNumber")
            .wasCreatedAtPrison("N/A")
            .wasUpdatedAtPrison("N/A")
        }
    }

    // Verify timeline event created
    await untilAsserted {
      val timeline = getTimeline(prisonNumber)
      assertThat(timeline).anyEvent { it.hasEventType(TimelineEventType.EDUCATION_ASSESSMENT_EVENT_CREATED) }
    }

    // Verify App Insights telemetry event sent
    await untilAsserted {
      val eventPropertiesCaptor = createCaptor<Map<String, String>>()
      verify(telemetryClient).trackEvent(
        eq("EDUCATION_ASSESSMENT_EVENT_CREATED"),
        capture(eventPropertiesCaptor),
        isNull(),
      )
      val eventProperties = eventPropertiesCaptor.value
      assertThat(eventProperties)
        .containsEntry("prisonNumber", prisonNumber)
        .containsEntry("status", "ALL_RELEVANT_ASSESSMENTS_COMPLETE")
        .containsEntry("source", "CURIOUS")
    }
  }
}
