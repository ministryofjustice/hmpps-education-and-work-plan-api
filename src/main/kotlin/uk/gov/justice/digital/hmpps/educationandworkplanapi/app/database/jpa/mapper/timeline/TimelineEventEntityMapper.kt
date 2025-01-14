package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.timeline

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.TimelineEventEntity
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext as TimelineEventContextDomain
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType as TimelineEventTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.TimelineEventContext as TimelineEventContextEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.TimelineEventType as TimelineEventTypeEntity

@Component
class TimelineEventEntityMapper {
  fun fromDomainToEntity(timelineEvent: TimelineEvent): TimelineEventEntity =
    with(timelineEvent) {
      TimelineEventEntity(
        reference = reference,
        sourceReference = sourceReference,
        eventType = toTimelineEventType(eventType),
        contextualInfo = contextualInfo.let { contextualInfo ->
          contextualInfo.keys.associate { toTimelineEventContext(it) to contextualInfo.getValue(it) }
        },
        prisonId = prisonId,
        actionedBy = actionedBy,
        timestamp = timestamp,
        correlationId = correlationId,
      )
    }

  fun fromEntityToDomain(persisted: TimelineEventEntity): TimelineEvent =
    with(persisted) {
      TimelineEvent(
        reference = reference,
        sourceReference = sourceReference,
        eventType = toTimelineEventType(eventType),
        contextualInfo = contextualInfo.let { contextualInfo ->
          contextualInfo.keys.associate { toTimelineEventContext(it) to contextualInfo.getValue(it) }
        },
        prisonId = prisonId,
        actionedBy = actionedBy,
        timestamp = timestamp,
        correlationId = correlationId,
      )
    }

  private fun toTimelineEventType(eventType: TimelineEventTypeEntity): TimelineEventTypeDomain =
    when (eventType) {
      TimelineEventTypeEntity.GOAL_CREATED -> TimelineEventTypeDomain.GOAL_CREATED
      TimelineEventTypeEntity.INDUCTION_CREATED -> TimelineEventTypeDomain.INDUCTION_CREATED
      TimelineEventTypeEntity.INDUCTION_UPDATED -> TimelineEventTypeDomain.INDUCTION_UPDATED
      TimelineEventTypeEntity.INDUCTION_SCHEDULE_CREATED -> TimelineEventTypeDomain.INDUCTION_SCHEDULE_CREATED
      TimelineEventTypeEntity.INDUCTION_SCHEDULE_UPDATED -> TimelineEventTypeDomain.INDUCTION_SCHEDULE_UPDATED
      TimelineEventTypeEntity.INDUCTION_SCHEDULE_STATUS_UPDATED -> TimelineEventTypeDomain.INDUCTION_SCHEDULE_STATUS_UPDATED
      TimelineEventTypeEntity.ACTION_PLAN_CREATED -> TimelineEventTypeDomain.ACTION_PLAN_CREATED
      TimelineEventTypeEntity.GOAL_UPDATED -> TimelineEventTypeDomain.GOAL_UPDATED
      TimelineEventTypeEntity.GOAL_COMPLETED -> TimelineEventTypeDomain.GOAL_COMPLETED
      TimelineEventTypeEntity.GOAL_ARCHIVED -> TimelineEventTypeDomain.GOAL_ARCHIVED
      TimelineEventTypeEntity.GOAL_UNARCHIVED -> TimelineEventTypeDomain.GOAL_UNARCHIVED
      TimelineEventTypeEntity.STEP_UPDATED -> TimelineEventTypeDomain.STEP_UPDATED
      TimelineEventTypeEntity.STEP_NOT_STARTED -> TimelineEventTypeDomain.STEP_NOT_STARTED
      TimelineEventTypeEntity.STEP_STARTED -> TimelineEventTypeDomain.STEP_STARTED
      TimelineEventTypeEntity.STEP_COMPLETED -> TimelineEventTypeDomain.STEP_COMPLETED
      TimelineEventTypeEntity.ACTION_PLAN_REVIEW_COMPLETED -> TimelineEventTypeDomain.ACTION_PLAN_REVIEW_COMPLETED
      TimelineEventTypeEntity.ACTION_PLAN_REVIEW_SCHEDULE_STATUS_UPDATED -> TimelineEventTypeDomain.ACTION_PLAN_REVIEW_SCHEDULE_STATUS_UPDATED
      TimelineEventTypeEntity.ACTION_PLAN_REVIEW_SCHEDULE_CREATED -> TimelineEventTypeDomain.ACTION_PLAN_REVIEW_SCHEDULE_CREATED
      TimelineEventTypeEntity.CONVERSATION_CREATED -> TimelineEventTypeDomain.CONVERSATION_CREATED
      TimelineEventTypeEntity.CONVERSATION_UPDATED -> TimelineEventTypeDomain.CONVERSATION_UPDATED
      TimelineEventTypeEntity.PRISON_ADMISSION -> TimelineEventTypeDomain.PRISON_ADMISSION
      TimelineEventTypeEntity.PRISON_RELEASE -> TimelineEventTypeDomain.PRISON_RELEASE
      TimelineEventTypeEntity.PRISON_TRANSFER -> TimelineEventTypeDomain.PRISON_TRANSFER
    }

  private fun toTimelineEventType(eventType: TimelineEventTypeDomain): TimelineEventTypeEntity =
    when (eventType) {
      TimelineEventTypeDomain.GOAL_CREATED -> TimelineEventTypeEntity.GOAL_CREATED
      TimelineEventTypeDomain.INDUCTION_CREATED -> TimelineEventTypeEntity.INDUCTION_CREATED
      TimelineEventTypeDomain.INDUCTION_UPDATED -> TimelineEventTypeEntity.INDUCTION_UPDATED
      TimelineEventTypeDomain.INDUCTION_SCHEDULE_CREATED -> TimelineEventTypeEntity.INDUCTION_SCHEDULE_CREATED
      TimelineEventTypeDomain.INDUCTION_SCHEDULE_UPDATED -> TimelineEventTypeEntity.INDUCTION_SCHEDULE_UPDATED
      TimelineEventTypeDomain.ACTION_PLAN_CREATED -> TimelineEventTypeEntity.ACTION_PLAN_CREATED
      TimelineEventTypeDomain.GOAL_UPDATED -> TimelineEventTypeEntity.GOAL_UPDATED
      TimelineEventTypeDomain.GOAL_COMPLETED -> TimelineEventTypeEntity.GOAL_COMPLETED
      TimelineEventTypeDomain.GOAL_ARCHIVED -> TimelineEventTypeEntity.GOAL_ARCHIVED
      TimelineEventTypeDomain.GOAL_UNARCHIVED -> TimelineEventTypeEntity.GOAL_UNARCHIVED
      TimelineEventTypeDomain.STEP_UPDATED -> TimelineEventTypeEntity.STEP_UPDATED
      TimelineEventTypeDomain.STEP_NOT_STARTED -> TimelineEventTypeEntity.STEP_NOT_STARTED
      TimelineEventTypeDomain.STEP_STARTED -> TimelineEventTypeEntity.STEP_STARTED
      TimelineEventTypeDomain.STEP_COMPLETED -> TimelineEventTypeEntity.STEP_COMPLETED
      TimelineEventTypeDomain.ACTION_PLAN_REVIEW_COMPLETED -> TimelineEventTypeEntity.ACTION_PLAN_REVIEW_COMPLETED
      TimelineEventTypeDomain.CONVERSATION_CREATED -> TimelineEventTypeEntity.CONVERSATION_CREATED
      TimelineEventTypeDomain.CONVERSATION_UPDATED -> TimelineEventTypeEntity.CONVERSATION_UPDATED
      TimelineEventTypeDomain.PRISON_ADMISSION -> TimelineEventTypeEntity.PRISON_ADMISSION
      TimelineEventTypeDomain.PRISON_RELEASE -> TimelineEventTypeEntity.PRISON_RELEASE
      TimelineEventTypeDomain.PRISON_TRANSFER -> TimelineEventTypeEntity.PRISON_TRANSFER
      TimelineEventTypeDomain.ACTION_PLAN_REVIEW_SCHEDULE_STATUS_UPDATED -> TimelineEventTypeEntity.ACTION_PLAN_REVIEW_SCHEDULE_STATUS_UPDATED
      TimelineEventTypeDomain.ACTION_PLAN_REVIEW_SCHEDULE_CREATED -> TimelineEventTypeEntity.ACTION_PLAN_REVIEW_SCHEDULE_CREATED
      TimelineEventTypeDomain.INDUCTION_SCHEDULE_STATUS_UPDATED -> TimelineEventTypeEntity.INDUCTION_SCHEDULE_STATUS_UPDATED
    }

  private fun toTimelineEventContext(eventType: TimelineEventContextEntity): TimelineEventContextDomain =
    when (eventType) {
      TimelineEventContextEntity.GOAL_TITLE -> TimelineEventContextDomain.GOAL_TITLE
      TimelineEventContextEntity.STEP_TITLE -> TimelineEventContextDomain.STEP_TITLE
      TimelineEventContextEntity.CONVERSATION_TYPE -> TimelineEventContextDomain.CONVERSATION_TYPE
      TimelineEventContextEntity.GOAL_ARCHIVED_REASON -> TimelineEventContextDomain.GOAL_ARCHIVED_REASON
      TimelineEventContextEntity.GOAL_ARCHIVED_REASON_OTHER -> TimelineEventContextDomain.GOAL_ARCHIVED_REASON_OTHER
      TimelineEventContextEntity.PRISON_TRANSFERRED_FROM -> TimelineEventContextDomain.PRISON_TRANSFERRED_FROM
      TimelineEventContextEntity.INDUCTION_SCHEDULE_STATUS -> TimelineEventContextDomain.INDUCTION_SCHEDULE_STATUS
      TimelineEventContextEntity.INDUCTION_SCHEDULE_DEADLINE_DATE -> TimelineEventContextDomain.INDUCTION_SCHEDULE_DEADLINE_DATE
      TimelineEventContextEntity.COMPLETED_REVIEW_ENTERED_ONLINE_AT -> TimelineEventContextDomain.COMPLETED_REVIEW_ENTERED_ONLINE_AT
      TimelineEventContextEntity.COMPLETED_REVIEW_ENTERED_ONLINE_BY -> TimelineEventContextDomain.COMPLETED_REVIEW_ENTERED_ONLINE_BY
      TimelineEventContextEntity.COMPLETED_REVIEW_NOTES -> TimelineEventContextDomain.COMPLETED_REVIEW_NOTES
      TimelineEventContextEntity.COMPLETED_REVIEW_CONDUCTED_IN_PERSON_DATE -> TimelineEventContextDomain.COMPLETED_REVIEW_CONDUCTED_IN_PERSON_DATE
      TimelineEventContextEntity.COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY -> TimelineEventContextDomain.COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY
      TimelineEventContextEntity.COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY_ROLE -> TimelineEventContextDomain.COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY_ROLE
      TimelineEventContextEntity.COMPLETED_INDUCTION_ENTERED_ONLINE_AT -> TimelineEventContextDomain.COMPLETED_INDUCTION_ENTERED_ONLINE_AT
      TimelineEventContextEntity.COMPLETED_INDUCTION_ENTERED_ONLINE_BY -> TimelineEventContextDomain.COMPLETED_INDUCTION_ENTERED_ONLINE_BY
      TimelineEventContextEntity.COMPLETED_INDUCTION_NOTES -> TimelineEventContextDomain.COMPLETED_INDUCTION_NOTES
      TimelineEventContextEntity.COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_DATE -> TimelineEventContextDomain.COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_DATE
      TimelineEventContextEntity.COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_BY -> TimelineEventContextDomain.COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_BY
      TimelineEventContextEntity.COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_BY_ROLE -> TimelineEventContextDomain.COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_BY_ROLE
      TimelineEventContextEntity.REVIEW_SCHEDULE_STATUS_OLD -> TimelineEventContextDomain.REVIEW_SCHEDULE_STATUS_OLD
      TimelineEventContextEntity.REVIEW_SCHEDULE_STATUS_NEW -> TimelineEventContextDomain.REVIEW_SCHEDULE_STATUS_NEW
      TimelineEventContextEntity.REVIEW_SCHEDULE_DEADLINE_OLD -> TimelineEventContextDomain.REVIEW_SCHEDULE_DEADLINE_OLD
      TimelineEventContextEntity.REVIEW_SCHEDULE_DEADLINE_NEW -> TimelineEventContextDomain.REVIEW_SCHEDULE_DEADLINE_NEW
      TimelineEventContextEntity.REVIEW_SCHEDULE_EXEMPTION_REASON -> TimelineEventContextDomain.REVIEW_SCHEDULE_EXEMPTION_REASON
      TimelineEventContextEntity.INDUCTION_SCHEDULE_STATUS_OLD -> TimelineEventContextDomain.INDUCTION_SCHEDULE_STATUS_OLD
      TimelineEventContextEntity.INDUCTION_SCHEDULE_STATUS_NEW -> TimelineEventContextDomain.INDUCTION_SCHEDULE_STATUS_NEW
      TimelineEventContextEntity.INDUCTION_SCHEDULE_DEADLINE_OLD -> TimelineEventContextDomain.INDUCTION_SCHEDULE_DEADLINE_OLD
      TimelineEventContextEntity.INDUCTION_SCHEDULE_DEADLINE_NEW -> TimelineEventContextDomain.INDUCTION_SCHEDULE_DEADLINE_NEW
      TimelineEventContextEntity.INDUCTION_SCHEDULE_EXEMPTION_REASON -> TimelineEventContextDomain.INDUCTION_SCHEDULE_EXEMPTION_REASON
    }

  private fun toTimelineEventContext(eventType: TimelineEventContextDomain): TimelineEventContextEntity =
    when (eventType) {
      TimelineEventContextDomain.GOAL_TITLE -> TimelineEventContextEntity.GOAL_TITLE
      TimelineEventContextDomain.STEP_TITLE -> TimelineEventContextEntity.STEP_TITLE
      TimelineEventContextDomain.CONVERSATION_TYPE -> TimelineEventContextEntity.CONVERSATION_TYPE
      TimelineEventContextDomain.GOAL_ARCHIVED_REASON -> TimelineEventContextEntity.GOAL_ARCHIVED_REASON
      TimelineEventContextDomain.GOAL_ARCHIVED_REASON_OTHER -> TimelineEventContextEntity.GOAL_ARCHIVED_REASON_OTHER
      TimelineEventContextDomain.PRISON_TRANSFERRED_FROM -> TimelineEventContextEntity.PRISON_TRANSFERRED_FROM
      TimelineEventContextDomain.INDUCTION_SCHEDULE_STATUS -> TimelineEventContextEntity.INDUCTION_SCHEDULE_STATUS
      TimelineEventContextDomain.INDUCTION_SCHEDULE_DEADLINE_DATE -> TimelineEventContextEntity.INDUCTION_SCHEDULE_DEADLINE_DATE
      TimelineEventContextDomain.COMPLETED_REVIEW_ENTERED_ONLINE_AT -> TimelineEventContextEntity.COMPLETED_REVIEW_ENTERED_ONLINE_AT
      TimelineEventContextDomain.COMPLETED_REVIEW_ENTERED_ONLINE_BY -> TimelineEventContextEntity.COMPLETED_REVIEW_ENTERED_ONLINE_BY
      TimelineEventContextDomain.COMPLETED_REVIEW_NOTES -> TimelineEventContextEntity.COMPLETED_REVIEW_NOTES
      TimelineEventContextDomain.COMPLETED_REVIEW_CONDUCTED_IN_PERSON_DATE -> TimelineEventContextEntity.COMPLETED_REVIEW_CONDUCTED_IN_PERSON_DATE
      TimelineEventContextDomain.COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY -> TimelineEventContextEntity.COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY
      TimelineEventContextDomain.COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY_ROLE -> TimelineEventContextEntity.COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY_ROLE
      TimelineEventContextDomain.COMPLETED_INDUCTION_ENTERED_ONLINE_AT -> TimelineEventContextEntity.COMPLETED_INDUCTION_ENTERED_ONLINE_AT
      TimelineEventContextDomain.COMPLETED_INDUCTION_ENTERED_ONLINE_BY -> TimelineEventContextEntity.COMPLETED_INDUCTION_ENTERED_ONLINE_BY
      TimelineEventContextDomain.COMPLETED_INDUCTION_NOTES -> TimelineEventContextEntity.COMPLETED_INDUCTION_NOTES
      TimelineEventContextDomain.COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_DATE -> TimelineEventContextEntity.COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_DATE
      TimelineEventContextDomain.COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_BY -> TimelineEventContextEntity.COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_BY
      TimelineEventContextDomain.COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_BY_ROLE -> TimelineEventContextEntity.COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_BY_ROLE
      TimelineEventContextDomain.REVIEW_SCHEDULE_STATUS_OLD -> TimelineEventContextEntity.REVIEW_SCHEDULE_STATUS_OLD
      TimelineEventContextDomain.REVIEW_SCHEDULE_STATUS_NEW -> TimelineEventContextEntity.REVIEW_SCHEDULE_STATUS_NEW
      TimelineEventContextDomain.REVIEW_SCHEDULE_DEADLINE_OLD -> TimelineEventContextEntity.REVIEW_SCHEDULE_DEADLINE_OLD
      TimelineEventContextDomain.REVIEW_SCHEDULE_DEADLINE_NEW -> TimelineEventContextEntity.REVIEW_SCHEDULE_DEADLINE_NEW
      TimelineEventContextDomain.REVIEW_SCHEDULE_EXEMPTION_REASON -> TimelineEventContextEntity.REVIEW_SCHEDULE_EXEMPTION_REASON
      TimelineEventContextDomain.INDUCTION_SCHEDULE_STATUS_OLD -> TimelineEventContextEntity.INDUCTION_SCHEDULE_STATUS_OLD
      TimelineEventContextDomain.INDUCTION_SCHEDULE_STATUS_NEW -> TimelineEventContextEntity.INDUCTION_SCHEDULE_STATUS_NEW
      TimelineEventContextDomain.INDUCTION_SCHEDULE_DEADLINE_OLD -> TimelineEventContextEntity.INDUCTION_SCHEDULE_DEADLINE_OLD
      TimelineEventContextDomain.INDUCTION_SCHEDULE_DEADLINE_NEW -> TimelineEventContextEntity.INDUCTION_SCHEDULE_DEADLINE_NEW
      TimelineEventContextDomain.INDUCTION_SCHEDULE_EXEMPTION_REASON -> TimelineEventContextEntity.INDUCTION_SCHEDULE_EXEMPTION_REASON
    }
}
