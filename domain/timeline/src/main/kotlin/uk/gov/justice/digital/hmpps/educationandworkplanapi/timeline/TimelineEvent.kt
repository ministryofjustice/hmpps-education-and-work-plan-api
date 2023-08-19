package uk.gov.justice.digital.hmpps.educationandworkplanapi.timeline

import java.time.Instant

/**
 * A TimelineEvent represents a single event that occurred in the system or potentially the wider prison estate.
 *
 * Once created a TimelineEvent cannot be mutated in any way. It represents a single event at a point in time.
 */
data class TimelineEvent(
  val title: String,
  val summary: String,
  val eventDateTime: Instant,
  val links: List<Link> = emptyList(),
)
