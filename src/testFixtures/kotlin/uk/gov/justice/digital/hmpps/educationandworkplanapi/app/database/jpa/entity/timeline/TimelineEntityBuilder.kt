package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import java.time.Instant
import java.util.UUID

fun aValidTimelineEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = aValidPrisonNumber(),
  events: MutableList<TimelineEventEntity> = mutableListOf(aValidTimelineEventEntity()),
  createdAt: Instant? = Instant.now(),
  updatedAt: Instant? = Instant.now(),
) = TimelineEntity(
  reference = reference,
  prisonNumber = prisonNumber,
  events = events,
).apply {
  this.id = id
  this.createdAt = createdAt
  this.updatedAt = updatedAt
}
