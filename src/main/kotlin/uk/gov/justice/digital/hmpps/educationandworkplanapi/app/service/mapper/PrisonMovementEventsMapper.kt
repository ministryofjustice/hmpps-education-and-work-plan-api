package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.mapper

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.PrisonMovementEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.PrisonMovementEvents
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.PrisonMovementType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.AuthAwareTokenConverter.Companion.SYSTEM_USER
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import java.util.UUID

@Component
class PrisonMovementEventsMapper(
  private val instantMapper: InstantMapper,
) {

  fun toTimelineEvents(prisonMovementEvents: PrisonMovementEvents): List<TimelineEvent> = prisonMovementEvents.prisonBookings.flatMap { booking ->
    booking.value.map { movement ->
      val bookingId = booking.key.toString()
      toTimelineEvent(bookingId = bookingId, prisonMovement = movement)
    }
  }

  private fun toTimelineEvent(bookingId: String, prisonMovement: PrisonMovementEvent): TimelineEvent = TimelineEvent(
    reference = UUID.randomUUID(),
    correlationId = UUID.randomUUID(),
    sourceReference = bookingId,
    eventType = toEventType(prisonMovement.movementType),
    timestamp = instantMapper.toInstant(prisonMovement.date)!!,
    contextualInfo = getContextualInfo(prisonMovement),
    prisonId = getPrisonId(prisonMovement),
    actionedBy = SYSTEM_USER,
  )

  private fun toEventType(movementType: PrisonMovementType): TimelineEventType = when (movementType) {
    PrisonMovementType.ADMISSION -> TimelineEventType.PRISON_ADMISSION
    PrisonMovementType.RELEASE -> TimelineEventType.PRISON_RELEASE
    PrisonMovementType.TRANSFER -> TimelineEventType.PRISON_TRANSFER
  }

  private fun getContextualInfo(prisonMovement: PrisonMovementEvent): Map<TimelineEventContext, String> = // For transfers, this is the ID of the prison they were transferred from. Otherwise, null
    if (prisonMovement.movementType == PrisonMovementType.TRANSFER) {
      mapOf(TimelineEventContext.PRISON_TRANSFERRED_FROM to prisonMovement.fromPrisonId!!)
    } else {
      emptyMap()
    }

  private fun getPrisonId(prisonMovement: PrisonMovementEvent): String = // Either the ID of the prison they entered (including transferred into), or the one they were released from
    when (prisonMovement.movementType) {
      PrisonMovementType.TRANSFER -> prisonMovement.toPrisonId!!
      PrisonMovementType.ADMISSION -> prisonMovement.toPrisonId!!
      PrisonMovementType.RELEASE -> prisonMovement.fromPrisonId!!
    }
}
