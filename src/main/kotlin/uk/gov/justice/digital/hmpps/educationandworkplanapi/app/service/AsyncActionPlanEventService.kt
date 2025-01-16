package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Induction
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlan
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.ActionPlanEventService
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService

private val log = KotlinLogging.logger {}

/**
 * Implementation of [ActionPlanEventService] for performing additional asynchronous actions related to [ActionPlan]
 * events.
 */
@Component
@Async
class AsyncActionPlanEventService(
  private val telemetryService: TelemetryService,
  private val timelineEventFactory: TimelineEventFactory,
  private val timelineService: TimelineService,
  private val inductionService: InductionService,
  private val userService: ManageUserService,

) : ActionPlanEventService {
  override fun actionPlanCreated(actionPlan: ActionPlan) {
    log.info { "ActionPlan created event for prisoner [${actionPlan.prisonNumber}]" }

    actionPlan.goals.forEach {
      telemetryService.trackGoalCreatedEvent(it)
    }

    val induction = inductionService.getInductionForPrisoner(actionPlan.prisonNumber)
    val inductionTimelineEvent = buildInductionCreatedEvent(induction)
    val timelineEvents = timelineEventFactory.actionPlanCreatedEvent(actionPlan, inductionTimelineEvent)

    timelineService.recordTimelineEvents(actionPlan.prisonNumber, timelineEvents)
  }

  private fun buildInductionCreatedEvent(induction: Induction): TimelineEvent =
    with(induction) {
      val noteContent = note?.content ?: ""
      val conductedByPerson = conductedBy ?: ""
      val conductedByRolePerson = conductedByRole ?: ""

      TimelineEvent.newTimelineEvent(
        sourceReference = reference.toString(),
        eventType = TimelineEventType.INDUCTION_CREATED,
        prisonId = createdAtPrison,
        actionedBy = induction.createdBy!!,
        timestamp = induction.createdAt!!,
        contextualInfo = mapOf(
          TimelineEventContext.COMPLETED_INDUCTION_ENTERED_ONLINE_AT to createdAt.toString(),
          TimelineEventContext.COMPLETED_INDUCTION_ENTERED_ONLINE_BY to userService.getUserDetails(createdBy!!).name,
          TimelineEventContext.COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_DATE to completedDate.toString(),
          TimelineEventContext.COMPLETED_INDUCTION_NOTES to noteContent,
          *conductedBy
            ?.let {
              arrayOf(
                TimelineEventContext.COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_BY to conductedByPerson,
                TimelineEventContext.COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_BY_ROLE to conductedByRolePerson,
              )
            } ?: arrayOf(),
        ),
      )
    }
}
