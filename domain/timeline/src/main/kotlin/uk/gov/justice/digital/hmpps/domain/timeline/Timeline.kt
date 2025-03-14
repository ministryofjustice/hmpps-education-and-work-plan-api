package uk.gov.justice.digital.hmpps.domain.timeline

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

  var events: MutableList<TimelineEvent>
    get() = field.also { events -> events.sortBy { it.timestamp } }

  init {
    this.events = events.toMutableList()
  }

  /**
   * Adds a [TimelineEvent] to the timeline.
   */
  fun addEvent(timelineEvent: TimelineEvent): Timeline {
    events.add(timelineEvent)
    return this
  }

  /**
   * Adds one or more [TimelineEvent]s to the timeline.
   */
  fun addEvents(timelineEvents: List<TimelineEvent>): Timeline {
    events.addAll(timelineEvents)
    return this
  }

  companion object {
    @JvmStatic
    fun newTimeline(prisonNumber: String, events: List<TimelineEvent>) =
      Timeline(UUID.randomUUID(), prisonNumber, events)
  }
}
