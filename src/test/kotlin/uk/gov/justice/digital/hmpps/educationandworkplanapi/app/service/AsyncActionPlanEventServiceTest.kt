package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.aValidTimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service.TimelineService

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
  fun actionPlanCreated() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val actionPlan = aValidActionPlan()
    val createActionPlanEvent = aValidTimelineEvent(eventType = TimelineEventType.ACTION_PLAN_CREATED)
    given(timelineEventFactory.actionPlanCreatedEvent(any())).willReturn(createActionPlanEvent)

    // When
    actionPlanEventService.actionPlanCreated(actionPlan)

    // Then
    verify(telemetryService).trackGoalCreateEvent(actionPlan.goals[0])
    verify(timelineEventFactory).actionPlanCreatedEvent(actionPlan)
    verify(timelineService).recordTimelineEvent(prisonNumber, createActionPlanEvent)
  }
}