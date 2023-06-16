package uk.gov.justice.digital.hmpps.educationandworkplanapi.testfixtures.domain.timeline

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.Link
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.Timeline
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent
import java.time.Instant

fun aValidTimeline(
  prisonNumber: String = "A1234AB",
  events: List<TimelineEvent> = listOf(aValidTimelineEvent()),
) = Timeline(
  prisonNumber = prisonNumber,
  events = events,
)

fun aValidTimelineEvent(
  title: String = "New Goal added",
  summary: String = "New education Goal added by Fred Blogs",
  eventDateTime: Instant = Instant.now(),
  links: List<Link> = listOf(aValidLink()),
) = TimelineEvent(
  title = title,
  summary = summary,
  eventDateTime = eventDateTime,
  links = links,
)

fun aValidLink(
  title: String = "Click here to see the Goal",
  location: String = "http://localhost/the-goal",
) = Link(
  title = title,
  location = location,
)
