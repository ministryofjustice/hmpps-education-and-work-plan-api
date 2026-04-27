package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.EmployabilitySkill
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.EmployabilitySkillType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.aValidEmployabilitySkill
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService

@ExtendWith(MockitoExtension::class)
class AsyncEmployabilitySkillsEventServiceTest {

  @InjectMocks
  private lateinit var employabilitySkillsEventService: AsyncEmployabilitySkillsEventService

  @Mock
  private lateinit var timelineService: TimelineService

  @Mock
  private lateinit var timelineEventFactory: TimelineEventFactory

  @Mock
  private lateinit var telemetryService: TelemetryService

  @Captor
  private lateinit var timelineEventCaptor: ArgumentCaptor<List<TimelineEvent>>

  @Test
  fun `should handle employability skills created`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()

    val communicationEmployabilitySkill = aValidEmployabilitySkill(prisonNumber = prisonNumber, employabilitySkillType = EmployabilitySkillType.COMMUNICATION)
    val adaptabilityEmployabilitySkill = aValidEmployabilitySkill(prisonNumber = prisonNumber, employabilitySkillType = EmployabilitySkillType.ADAPTABILITY)
    val problemSolvingEmployabilitySkill = aValidEmployabilitySkill(prisonNumber = prisonNumber, employabilitySkillType = EmployabilitySkillType.PROBLEM_SOLVING)
    val employabilitySkills = listOf(
      communicationEmployabilitySkill,
      adaptabilityEmployabilitySkill,
      problemSolvingEmployabilitySkill,
    )

    val expectedCommunicationTimelineEvent =
      with(communicationEmployabilitySkill) {
        TimelineEvent.newTimelineEvent(
          sourceReference = reference.toString(),
          eventType = TimelineEventType.EMPLOYABILITY_SKILL_CREATED,
          prisonId = createdAtPrison,
          actionedBy = createdBy,
          timestamp = createdAt,
          contextualInfo = mapOf(
            TimelineEventContext.EMPLOYABILITY_SKILL_TYPE to employabilitySkillType.toString(),
          ),
        )
      }
    val expectedAdaptabilityTimelineEvent =
      with(adaptabilityEmployabilitySkill) {
        TimelineEvent.newTimelineEvent(
          sourceReference = reference.toString(),
          eventType = TimelineEventType.EMPLOYABILITY_SKILL_CREATED,
          prisonId = createdAtPrison,
          actionedBy = createdBy,
          timestamp = createdAt,
          contextualInfo = mapOf(
            TimelineEventContext.EMPLOYABILITY_SKILL_TYPE to employabilitySkillType.toString(),
          ),
        )
      }
    val expectedProblemSolvingTimelineEvent =
      with(problemSolvingEmployabilitySkill) {
        TimelineEvent.newTimelineEvent(
          sourceReference = reference.toString(),
          eventType = TimelineEventType.EMPLOYABILITY_SKILL_CREATED,
          prisonId = createdAtPrison,
          actionedBy = createdBy,
          timestamp = createdAt,
          contextualInfo = mapOf(
            TimelineEventContext.EMPLOYABILITY_SKILL_TYPE to employabilitySkillType.toString(),
          ),
        )
      }
    val expectedTimelineEvents = listOf(expectedCommunicationTimelineEvent, expectedAdaptabilityTimelineEvent, expectedProblemSolvingTimelineEvent)
    given(timelineEventFactory.employabilitySkillsCreatedTimelineEvents(any(), any())).willReturn(expectedTimelineEvents)

    // When
    employabilitySkillsEventService.employabilitySkillsCreated(employabilitySkills)

    // Then
    verify(timelineService).recordTimelineEvents(eq(prisonNumber), capture(timelineEventCaptor))
    assertThat(timelineEventCaptor.value).isEqualTo(expectedTimelineEvents)
    verify(timelineEventFactory).employabilitySkillsCreatedTimelineEvents(eq(employabilitySkills), any())

    verify(telemetryService).trackEmployabilitySkillCreated(communicationEmployabilitySkill)
    verify(telemetryService).trackEmployabilitySkillCreated(adaptabilityEmployabilitySkill)
    verify(telemetryService).trackEmployabilitySkillCreated(problemSolvingEmployabilitySkill)
  }

  @Test
  fun `should handle no employability skills created`() {
    // Given
    val employabilitySkills = emptyList<EmployabilitySkill>()

    // When
    employabilitySkillsEventService.employabilitySkillsCreated(employabilitySkills)

    // Then
    verifyNoInteractions(timelineService)
    verifyNoInteractions(telemetryService)
  }
}
