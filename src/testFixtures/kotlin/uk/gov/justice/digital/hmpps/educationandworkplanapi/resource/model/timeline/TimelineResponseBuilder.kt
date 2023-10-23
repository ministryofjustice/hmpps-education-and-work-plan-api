package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.timeline

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineResponse
import java.util.UUID

fun aValidTimelineResponse(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "",
  events: List<TimelineEventResponse> = listOf(aValidTimelineEventResponse()),
): TimelineResponse =
  TimelineResponse(
    reference = reference,
    prisonNumber = prisonNumber,
    events = events,
  )
