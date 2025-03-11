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
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidActionPlan
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.domain.timeline.aValidTimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService

@ExtendWith(MockitoExtension::class)
class AsyncActionPlanEventServiceTest {

  @InjectMocks
  private lateinit var actionPlanEventService: AsyncActionPlanEventService

  @Mock
  private lateinit var telemetryService: TelemetryService

  @Mock
  private lateinit var timelineEventFactory: TimelineEventFactory

  @Mock
  private lateinit var timelineService: TimelineService

  @Test
  fun `should handle action plan created`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val actionPlan = aValidActionPlan(prisonNumber = prisonNumber)
    val createActionPlanEvents = listOf(
      aValidTimelineEvent(eventType = TimelineEventType.ACTION_PLAN_CREATED),
      aValidTimelineEvent(eventType = TimelineEventType.GOAL_CREATED),
    )
    given(timelineEventFactory.actionPlanCreatedEvent(actionPlan)).willReturn(createActionPlanEvents)

    // When
    actionPlanEventService.actionPlanCreated(actionPlan)

    // Then
    verify(telemetryService).trackGoalCreatedEvent(eq(actionPlan.goals[0]), any())
    verify(timelineEventFactory).actionPlanCreatedEvent(actionPlan)
    verify(timelineService).recordTimelineEvents(prisonNumber, createActionPlanEvents)
  }
}
