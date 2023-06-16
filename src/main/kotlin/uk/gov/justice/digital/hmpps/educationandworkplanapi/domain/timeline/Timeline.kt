package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline

/**
 * A Timeline is sequenced list of [TimelineEvent]s for a given prisoner. The list can be empty, representing an empty
 * timeline for the prisoner.
 *
 * The list of events cannot be mutated directly, and instead should be mutated through methods such as [addEvent]
 */
class Timeline(
  val prisonNumber: String,
  events: List<TimelineEvent> = emptyList(),
) {

  // _events is the real property and is deliberately private to prevent external access and mutation.
  // events is a pseudo property that returns the current _events field as a List (ie. non-mutable)
  private val _events: MutableList<TimelineEvent>
  val events: List<TimelineEvent>
    get() = _events.toList()

  init {
    _events = events.toMutableList().apply { sortByDescending { it.eventDateTime } }
  }

  /**
   * Adds a [TimelineEvent] to the timeline, ensuring that the events are
   * in chronological order
   */
  fun addEvent(timelineEvent: TimelineEvent) {
    with(_events) {
      add(timelineEvent)
      sortByDescending { it.eventDateTime }
    }
  }
}
