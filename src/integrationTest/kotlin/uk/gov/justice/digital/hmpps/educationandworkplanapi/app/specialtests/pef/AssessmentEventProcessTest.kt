package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.specialtests.pef

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilAsserted
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.aValidSqsAssessmentEventMessage
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.validMessageAttributes
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.COMPLETED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.timeline.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.LocalDate

@Isolated
@ActiveProfiles("integration-test", "ciag-kpi-pef-rules")
class AssessmentEventProcessTest : IntegrationTestBase() {

  @Test
  fun `should process assessment event message and persist record given prisoner has a scheduled induction`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()
    createInductionSchedule(
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.SCHEDULED,
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

    // Verify record persisted in database
    await untilAsserted {
      val events = educationAssessmentEventRepository.findByPrisonNumber(prisonNumber)
      assertThat(events)
        .hasNumberOfEvents(1)
    }

    // assert that the induction schedule is still SCHEDULED
    val inductionSchedule = getInductionSchedule(prisonNumber)
    assertThat(inductionSchedule)
      .wasStatus(SCHEDULED)
  }

  @Test
  fun `should process assessment event message and persist record given prisoner has an exempt induction`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()
    createInductionSchedule(
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY,
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

    // Verify record persisted in database
    await untilAsserted {
      val events = educationAssessmentEventRepository.findByPrisonNumber(prisonNumber)
      assertThat(events)
        .hasNumberOfEvents(1)
    }

    // assert that the induction schedule is still SCHEDULED
    val inductionSchedule = getInductionSchedule(prisonNumber)
    assertThat(inductionSchedule)
      .wasStatus(EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY)
  }

  @Test
  fun `should process assessment event message and persist record given prisoner has a complete induction`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()
    createInductionSchedule(
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.COMPLETED,
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

    // Verify record persisted in database
    await untilAsserted {
      val events = educationAssessmentEventRepository.findByPrisonNumber(prisonNumber)
      assertThat(events)
        .hasNumberOfEvents(1)
    }

    // assert that the induction schedule is still SCHEDULED
    val inductionSchedule = getInductionSchedule(prisonNumber)
    assertThat(inductionSchedule)
      .wasStatus(COMPLETED)
  }

  @Test
  fun `should process assessment event message and persist record`() {
    // Given
    val prisonNumber = setUpRandomPrisoner()
    val statusChangeDate = LocalDate.now()

    val sqsMessage = aValidSqsAssessmentEventMessage(
      messageAttributes = validMessageAttributes(
        prisonNumber = prisonNumber,
        statusChangeDate = statusChangeDate,
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
          it.hasPrisonNumber(prisonNumber)
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
      assertThat(timeline)
        .anyEvent { it.hasEventType(TimelineEventType.EDUCATION_ASSESSMENT_EVENT_CREATED) }
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
    val prisonNumber = setUpRandomPrisoner()

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
    val prisonNumber = setUpRandomPrisoner()

    wiremockService.stubGetPrisonerNotFound(prisonNumber)

    val statusChangeDate = LocalDate.now()
    val sqsMessage = aValidSqsAssessmentEventMessage(
      messageAttributes = validMessageAttributes(
        prisonNumber = prisonNumber,
        statusChangeDate = statusChangeDate,
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
          it.hasPrisonNumber(prisonNumber)
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
      assertThat(timeline)
        .anyEvent { it.hasEventType(TimelineEventType.EDUCATION_ASSESSMENT_EVENT_CREATED) }
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
