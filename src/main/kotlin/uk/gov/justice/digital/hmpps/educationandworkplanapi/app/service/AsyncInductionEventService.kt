package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Induction
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.service.InductionEventService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.service.TimelineService

private val log = KotlinLogging.logger {}

/**
 * Implementation of [InductionEventService] for performing additional asynchronous actions related to [Induction] events.
 */
@Component
class AsyncInductionEventService(
  private val timelineService: TimelineService,
) : InductionEventService {

  override fun inductionCreated(createdInduction: Induction) {
    runBlocking {
      log.debug { "Induction created event for prisoner [${createdInduction.prisonNumber}]" }
      timelineService.recordTimelineEvent(
        createdInduction.prisonNumber,
        buildInductionCreatedEvent(createdInduction),
      )
    }
  }

  override fun inductionUpdated(updatedInduction: Induction) {
    runBlocking {
      log.debug { "Induction updated event for prisoner [${updatedInduction.prisonNumber}]" }
      timelineService.recordTimelineEvent(
        updatedInduction.prisonNumber,
        buildInductionUpdatedEvent(updatedInduction),
      )
    }
  }

  private fun buildInductionCreatedEvent(induction: Induction): TimelineEvent =
    with(induction) {
      TimelineEvent.newTimelineEvent(
        sourceReference = reference.toString(),
        eventType = TimelineEventType.INDUCTION_CREATED,
        prisonId = createdAtPrison,
        actionedBy = induction.createdBy!!,
        actionedByDisplayName = induction.createdByDisplayName,
        timestamp = induction.createdAt!!,
      )
    }

  private fun buildInductionUpdatedEvent(induction: Induction): TimelineEvent =
    with(induction) {
      TimelineEvent.newTimelineEvent(
        sourceReference = reference.toString(),
        eventType = TimelineEventType.INDUCTION_UPDATED,
        prisonId = lastUpdatedAtPrison,
        actionedBy = induction.lastUpdatedBy!!,
        actionedByDisplayName = induction.lastUpdatedByDisplayName,
        timestamp = induction.lastUpdatedAt!!,
      )
    }
}
