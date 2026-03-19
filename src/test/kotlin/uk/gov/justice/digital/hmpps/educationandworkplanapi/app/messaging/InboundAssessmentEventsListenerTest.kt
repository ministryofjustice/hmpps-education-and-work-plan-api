package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.educationassessment.EducationAssessmentEventEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.EducationAssessmentEventRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.TelemetryService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.TimelineEventFactory
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class InboundAssessmentEventsListenerTest {

  @Mock
  private lateinit var educationAssessmentEventRepository: EducationAssessmentEventRepository

  @Mock
  private lateinit var educationAssessmentEventEntityMapper: EducationAssessmentEventEntityMapper

  @Mock
  private lateinit var prisonerSearchApiService: PrisonerSearchApiService

  @Mock
  private lateinit var timelineService: TimelineService

  @Mock
  private lateinit var timelineEventFactory: TimelineEventFactory

  @Mock
  private lateinit var telemetryService: TelemetryService

  @InjectMocks
  private lateinit var inboundAssessmentEventsListener: InboundAssessmentEventsListener

  @Test
  fun `should process assessment event and save to repository and record timeline event`() {
    // Given
    val prisonNumber = "G0378GI"
    val statusChangeDate = LocalDate.of(2026, 3, 15)
    val sqsMessage = SqsAssessmentEventMessage(
      messageId = "14e2865f-1e4b-43b0-87e8-874e7e238dd9",
      eventType = "EducationAssessmentEventCreated",
      description = null,
      who = null,
      messageAttributes = MessageAttributes(
        prisonNumber = prisonNumber,
        status = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = statusChangeDate,
        detailUrl = "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/G0378GI",
        requestId = "0650ba37-a977-4fbe-9000-4715aaecadba",
      ),
    )

    val prisoner = aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "BXI")
    given(prisonerSearchApiService.getPrisoner(prisonNumber)).willReturn(prisoner)

    val entityReference = UUID.randomUUID()
    val expectedEntity = EducationAssessmentEventEntity(
      reference = entityReference,
      prisonNumber = prisonNumber,
      status = EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
      statusChangeDate = statusChangeDate,
      source = "CURIOUS",
      detailUrl = "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/G0378GI",
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )
    given(educationAssessmentEventEntityMapper.fromMessageToEntity(any(), eq("BXI"))).willReturn(expectedEntity)
    given(educationAssessmentEventRepository.saveAndFlush(any<EducationAssessmentEventEntity>())).willReturn(expectedEntity)

    val expectedTimelineEvent = TimelineEvent.newTimelineEvent(
      sourceReference = entityReference.toString(),
      eventType = TimelineEventType.EDUCATION_ASSESSMENT_EVENT_CREATED,
      prisonId = "BXI",
      actionedBy = "system",
    )
    given(timelineEventFactory.educationAssessmentEventCreatedEvent(any(), any())).willReturn(expectedTimelineEvent)

    // When
    inboundAssessmentEventsListener.onMessage(sqsMessage)

    // Then
    verify(prisonerSearchApiService).getPrisoner(prisonNumber)
    verify(educationAssessmentEventEntityMapper).fromMessageToEntity(sqsMessage, "BXI")
    verify(educationAssessmentEventRepository).saveAndFlush(expectedEntity)
    verify(timelineEventFactory).educationAssessmentEventCreatedEvent(entityReference.toString(), "BXI")
    verify(timelineService).recordTimelineEvent(prisonNumber, expectedTimelineEvent)
    verify(telemetryService).trackEducationAssessmentEventCreated(expectedEntity)
  }

  @Test
  fun `should use fallback prison ID when prisoner has no prison`() {
    // Given
    val prisonNumber = "G0378GI"
    val sqsMessage = SqsAssessmentEventMessage(
      messageId = "14e2865f-1e4b-43b0-87e8-874e7e238dd9",
      eventType = "EducationAssessmentEventCreated",
      messageAttributes = MessageAttributes(
        prisonNumber = prisonNumber,
        status = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.now(),
        detailUrl = null,
        requestId = "0650ba37-a977-4fbe-9000-4715aaecadba",
      ),
    )

    val prisoner = aValidPrisoner(prisonerNumber = prisonNumber, prisonId = null)
    given(prisonerSearchApiService.getPrisoner(prisonNumber)).willReturn(prisoner)

    val entityReference = UUID.randomUUID()
    val expectedEntity = EducationAssessmentEventEntity(
      reference = entityReference,
      prisonNumber = prisonNumber,
      status = EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
      statusChangeDate = LocalDate.now(),
      source = "CURIOUS",
      detailUrl = null,
      createdAtPrison = "N/A",
      updatedAtPrison = "N/A",
    )
    given(educationAssessmentEventEntityMapper.fromMessageToEntity(any(), eq("N/A"))).willReturn(expectedEntity)
    given(educationAssessmentEventRepository.saveAndFlush(any<EducationAssessmentEventEntity>())).willReturn(expectedEntity)

    val expectedTimelineEvent = TimelineEvent.newTimelineEvent(
      sourceReference = entityReference.toString(),
      eventType = TimelineEventType.EDUCATION_ASSESSMENT_EVENT_CREATED,
      prisonId = "N/A",
      actionedBy = "system",
    )
    given(timelineEventFactory.educationAssessmentEventCreatedEvent(any(), any())).willReturn(expectedTimelineEvent)

    // When
    inboundAssessmentEventsListener.onMessage(sqsMessage)

    // Then
    verify(educationAssessmentEventEntityMapper).fromMessageToEntity(sqsMessage, "N/A")
    verify(educationAssessmentEventRepository).saveAndFlush(expectedEntity)
    verify(timelineEventFactory).educationAssessmentEventCreatedEvent(entityReference.toString(), "N/A")
    verify(timelineService).recordTimelineEvent(prisonNumber, expectedTimelineEvent)
    verify(telemetryService).trackEducationAssessmentEventCreated(expectedEntity)
  }
}
