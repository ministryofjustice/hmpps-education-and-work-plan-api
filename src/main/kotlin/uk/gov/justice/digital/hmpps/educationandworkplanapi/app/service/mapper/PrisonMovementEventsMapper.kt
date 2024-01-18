package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.ValueMapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.PrisonMovementEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.PrisonMovementEvents
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.PrisonMovementType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEventType
import java.time.Instant
import java.util.UUID

@Mapper(
  uses = [
    InstantMapper::class,
  ],
  imports = [
    Instant::class,
    UUID::class,
  ],
)
abstract class PrisonMovementEventsMapper {
  fun toTimelineEvents(prisonMovementEvents: PrisonMovementEvents): List<TimelineEvent> {
    val timelineEvents = mutableListOf<TimelineEvent>()
    prisonMovementEvents.prisonMovements.map { booking ->
      booking.value.forEach { movement ->
        val bookingId = booking.key.toString()
        timelineEvents.add(
          toTimelineEvent(
            bookingId = bookingId,
            prisonMovement = movement,
          ),
        )
      }
    }
    return timelineEvents
  }

  @Mapping(target = "reference", expression = "java( java.util.UUID.randomUUID() )")
  @Mapping(target = "sourceReference", source = "bookingId")
  @Mapping(target = "eventType", source = "prisonMovement.movementType")
  @Mapping(target = "contextualInfo", expression = "java( getContextualInfo(prisonMovement) )")
  @Mapping(target = "prisonId", expression = "java( getPrisonId(prisonMovement) )")
  @Mapping(target = "actionedBy", constant = "system")
  @Mapping(target = "actionedByDisplayName", ignore = true)
  @Mapping(target = "timestamp", source = "prisonMovement.date")
  @Mapping(target = "correlationId", expression = "java( java.util.UUID.randomUUID() )")
  abstract fun toTimelineEvent(bookingId: String, prisonMovement: PrisonMovementEvent): TimelineEvent

  @ValueMapping(target = "PRISON_ADMISSION", source = "ADMISSION")
  @ValueMapping(target = "PRISON_RELEASE", source = "RELEASE")
  @ValueMapping(target = "PRISON_TRANSFER", source = "TRANSFER")
  abstract fun toEventType(movementType: PrisonMovementType): TimelineEventType

  protected fun getContextualInfo(prisonMovement: PrisonMovementEvent): String? =
    // For transfers, this is the ID of the prison they were transferred from. Otherwise, null
    if (prisonMovement.movementType == PrisonMovementType.TRANSFER) {
      prisonMovement.fromPrisonId
    } else {
      null
    }

  protected fun getPrisonId(prisonMovement: PrisonMovementEvent): String =
    // Either the ID of the prison they entered (including transferred into), or the one they were released from
    when (prisonMovement.movementType) {
      PrisonMovementType.TRANSFER -> prisonMovement.toPrisonId!!
      PrisonMovementType.ADMISSION -> prisonMovement.toPrisonId!!
      PrisonMovementType.RELEASE -> prisonMovement.fromPrisonId!!
    }
}
