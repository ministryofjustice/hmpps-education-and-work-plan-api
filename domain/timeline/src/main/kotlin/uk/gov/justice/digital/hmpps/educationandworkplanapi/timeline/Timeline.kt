package uk.gov.justice.digital.hmpps.educationandworkplanapi.timeline

/**
 * A Timeline is sequenced list of [TimelineEvent]s for a given prisoner. The list can be empty, representing an empty
 * timeline for the prisoner.
 */
class Timeline(
  val prisonNumber: String,
  events: List<TimelineEvent> = emptyList(),
) {

  val events: MutableList<TimelineEvent>
    get() = field.also { events -> events.sortByDescending { it.eventDateTime } }

  init {
    this.events = events.toMutableList()
  }

  /**
   * Adds a [TimelineEvent] to the timeline.
   */
  fun addEvent(timelineEvent: TimelineEvent) =
    events.add(timelineEvent)
}
