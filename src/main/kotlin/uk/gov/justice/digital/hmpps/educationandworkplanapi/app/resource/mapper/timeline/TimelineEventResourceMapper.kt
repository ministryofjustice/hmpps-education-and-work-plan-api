package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.timeline

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.AuthAwareTokenConverter.Companion.SYSTEM_USER
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventResponse
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType as TimelineEventTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType as TimelineEventTypeApi

@Component
class TimelineEventResourceMapper(
  private val instantMapper: InstantMapper,
  private val userService: ManageUserService,
) {
  fun fromDomainToModel(timelineEventDomain: TimelineEvent): TimelineEventResponse =
    with(timelineEventDomain) {
      TimelineEventResponse(
        reference = reference,
        sourceReference = sourceReference,
        correlationId = correlationId,
        eventType = toTimelineEventType(eventType),
        prisonId = prisonId,
        contextualInfo = buildContextualInfo(contextualInfo),
        actionedBy = actionedBy,
        timestamp = instantMapper.toOffsetDateTime(timestamp)!!,
        actionedByDisplayName = if (actionedBy !== SYSTEM_USER) userService.getUserDetails(actionedBy).name else null,
      )
    }

  private fun buildContextualInfo(contextualInfo: Map<TimelineEventContext, String>): Map<String, String> =
    contextualInfo.mapKeys { it.key.toString() } ?: emptyMap()

  private fun toTimelineEventType(timelineEventType: TimelineEventTypeDomain): TimelineEventTypeApi =
    when (timelineEventType) {
      TimelineEventTypeDomain.INDUCTION_CREATED -> TimelineEventTypeApi.INDUCTION_CREATED
      TimelineEventTypeDomain.INDUCTION_UPDATED -> TimelineEventTypeApi.INDUCTION_UPDATED
      TimelineEventTypeDomain.INDUCTION_SCHEDULE_CREATED -> TimelineEventTypeApi.INDUCTION_SCHEDULE_CREATED
      TimelineEventTypeDomain.INDUCTION_SCHEDULE_UPDATED -> TimelineEventTypeApi.INDUCTION_SCHEDULE_UPDATED
      TimelineEventTypeDomain.INDUCTION_SCHEDULE_STATUS_UPDATED -> TimelineEventTypeApi.INDUCTION_SCHEDULE_STATUS_UPDATED
      TimelineEventTypeDomain.ACTION_PLAN_CREATED -> TimelineEventTypeApi.ACTION_PLAN_CREATED
      TimelineEventTypeDomain.GOAL_CREATED -> TimelineEventTypeApi.GOAL_CREATED
      TimelineEventTypeDomain.GOAL_UPDATED -> TimelineEventTypeApi.GOAL_UPDATED
      TimelineEventTypeDomain.GOAL_COMPLETED -> TimelineEventTypeApi.GOAL_COMPLETED
      TimelineEventTypeDomain.GOAL_ARCHIVED -> TimelineEventTypeApi.GOAL_ARCHIVED
      TimelineEventTypeDomain.GOAL_UNARCHIVED -> TimelineEventTypeApi.GOAL_UNARCHIVED
      TimelineEventTypeDomain.STEP_UPDATED -> TimelineEventTypeApi.STEP_UPDATED
      TimelineEventTypeDomain.STEP_NOT_STARTED -> TimelineEventTypeApi.STEP_NOT_STARTED
      TimelineEventTypeDomain.STEP_STARTED -> TimelineEventTypeApi.STEP_STARTED
      TimelineEventTypeDomain.STEP_COMPLETED -> TimelineEventTypeApi.STEP_COMPLETED
      TimelineEventTypeDomain.ACTION_PLAN_REVIEW_COMPLETED -> TimelineEventTypeApi.ACTION_PLAN_REVIEW_COMPLETED
      TimelineEventTypeDomain.ACTION_PLAN_REVIEW_SCHEDULE_CREATED -> TimelineEventTypeApi.ACTION_PLAN_REVIEW_SCHEDULE_CREATED
      TimelineEventTypeDomain.ACTION_PLAN_REVIEW_SCHEDULE_STATUS_UPDATED -> TimelineEventTypeApi.ACTION_PLAN_REVIEW_SCHEDULE_STATUS_UPDATED
      TimelineEventTypeDomain.PRISON_ADMISSION -> TimelineEventTypeApi.PRISON_ADMISSION
      TimelineEventTypeDomain.PRISON_RELEASE -> TimelineEventTypeApi.PRISON_RELEASE
      TimelineEventTypeDomain.PRISON_TRANSFER -> TimelineEventTypeApi.PRISON_TRANSFER
    }
}
