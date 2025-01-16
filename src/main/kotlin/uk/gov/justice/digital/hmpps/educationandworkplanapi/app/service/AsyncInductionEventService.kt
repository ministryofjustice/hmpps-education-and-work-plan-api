package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Induction
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionEventService
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService

private val log = KotlinLogging.logger {}

/**
 * Implementation of [InductionEventService] for performing additional asynchronous actions related to [Induction] events.
 */
@Component
@Async
class AsyncInductionEventService(
  private val timelineService: TimelineService,
  private val telemetryService: TelemetryService,
  private val userService: ManageUserService,
) : InductionEventService {

  override fun inductionCreated(createdInduction: Induction) {
    log.debug { "Induction created event for prisoner [${createdInduction.prisonNumber}]" }
    timelineService.recordTimelineEvent(createdInduction.prisonNumber, buildInductionCreatedEvent(createdInduction))
    telemetryService.trackInductionCreated(induction = createdInduction)
  }

  override fun inductionUpdated(updatedInduction: Induction) {
    log.debug { "Induction updated event for prisoner [${updatedInduction.prisonNumber}]" }
    timelineService.recordTimelineEvent(updatedInduction.prisonNumber, buildInductionUpdatedEvent(updatedInduction))
    telemetryService.trackInductionUpdated(induction = updatedInduction)
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

  private fun buildInductionUpdatedEvent(induction: Induction): TimelineEvent =
    with(induction) {
      TimelineEvent.newTimelineEvent(
        sourceReference = reference.toString(),
        eventType = TimelineEventType.INDUCTION_UPDATED,
        prisonId = lastUpdatedAtPrison,
        actionedBy = induction.lastUpdatedBy!!,
        timestamp = induction.lastUpdatedAt!!,
      )
    }
}
