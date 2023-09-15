package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline

import java.util.UUID

/**
 * A Timeline is sequenced list of [TimelineEvent]s for a given prisoner. The list can be empty, representing an empty
 * timeline for the prisoner.
 */
class Timeline(
  val reference: UUID,
  val prisonNumber: String,
  events: List<TimelineEvent> = emptyList(),
) {

  val events: MutableList<TimelineEvent>
    get() = field.also { events -> events.sortBy { it.timestamp } }

  init {
    this.events = events.toMutableList()
  }

  /**
   * Adds a [TimelineEvent] to the timeline.
   */
  fun addEvent(timelineEvent: TimelineEvent) =
    events.add(timelineEvent)
}
