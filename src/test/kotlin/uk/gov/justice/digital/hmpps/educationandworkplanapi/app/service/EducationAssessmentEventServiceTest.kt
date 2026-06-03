package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.educationassessment.EducationAssessmentEventEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.EducationAssessmentEventRepository
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class EducationAssessmentEventServiceTest {

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

  @Mock
  private lateinit var inductionScheduleService: InductionScheduleService

  @InjectMocks
  private lateinit var service: EducationAssessmentEventService

  @Test
  fun `should process assessment event - lookup prisoner, persist entity, record timeline, track telemetry`() {
    // Given
    val prisonNumber = "G0378GI"
    val statusChangeDate = LocalDate.of(2026, 3, 15)
    val dto = AssessmentEventDto(
      prisonNumber = prisonNumber,
      status = AssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
      statusChangeDate = statusChangeDate,
      detailUrl = "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/G0378GI",
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
    given(educationAssessmentEventEntityMapper.fromDtoToEntity(any(), eq("BXI"))).willReturn(expectedEntity)
    given(educationAssessmentEventRepository.saveAndFlush(any<EducationAssessmentEventEntity>())).willReturn(expectedEntity)

    val expectedTimelineEvent = TimelineEvent.newTimelineEvent(
      sourceReference = entityReference.toString(),
      eventType = TimelineEventType.EDUCATION_ASSESSMENT_EVENT_CREATED,
      prisonId = "BXI",
      actionedBy = "system",
    )
    given(timelineEventFactory.educationAssessmentEventCreatedEvent(any(), any())).willReturn(expectedTimelineEvent)

    // When
    service.process(dto)

    // Then
    verify(prisonerSearchApiService).getPrisoner(prisonNumber)
    verify(educationAssessmentEventEntityMapper).fromDtoToEntity(dto, "BXI")
    verify(educationAssessmentEventRepository).saveAndFlush(expectedEntity)
    verify(inductionScheduleService).schedulePendingInductionSchedule(prisonNumber, "BXI")
    verify(timelineEventFactory).educationAssessmentEventCreatedEvent(entityReference.toString(), "BXI")
    verify(timelineService).recordTimelineEvent(prisonNumber, expectedTimelineEvent)
    verify(telemetryService).trackEducationAssessmentEventCreated(expectedEntity)
  }

  @Test
  fun `should use fallback prison ID when prisoner has no prison`() {
    // Given
    val prisonNumber = "G0378GI"
    val dto = AssessmentEventDto(
      prisonNumber = prisonNumber,
      status = AssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
      statusChangeDate = LocalDate.now(),
      detailUrl = null,
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
    given(educationAssessmentEventEntityMapper.fromDtoToEntity(any(), eq("N/A"))).willReturn(expectedEntity)
    given(educationAssessmentEventRepository.saveAndFlush(any<EducationAssessmentEventEntity>())).willReturn(expectedEntity)

    val expectedTimelineEvent = TimelineEvent.newTimelineEvent(
      sourceReference = entityReference.toString(),
      eventType = TimelineEventType.EDUCATION_ASSESSMENT_EVENT_CREATED,
      prisonId = "N/A",
      actionedBy = "system",
    )
    given(timelineEventFactory.educationAssessmentEventCreatedEvent(any(), any())).willReturn(expectedTimelineEvent)

    // When
    service.process(dto)

    // Then
    verify(educationAssessmentEventEntityMapper).fromDtoToEntity(dto, "N/A")
    verify(educationAssessmentEventRepository).saveAndFlush(expectedEntity)
    verify(timelineEventFactory).educationAssessmentEventCreatedEvent(entityReference.toString(), "N/A")
    verify(timelineService).recordTimelineEvent(prisonNumber, expectedTimelineEvent)
    verify(telemetryService).trackEducationAssessmentEventCreated(expectedEntity)
  }
}
