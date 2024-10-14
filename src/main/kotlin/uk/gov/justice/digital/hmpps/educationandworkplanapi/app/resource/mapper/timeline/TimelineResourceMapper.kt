package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.timeline

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.timeline.Timeline
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineResponse

@Component
class TimelineResourceMapper(
  private val timelineEventResourceMapper: TimelineEventResourceMapper,
) {
  fun fromDomainToModel(timeline: Timeline): TimelineResponse =
    with(timeline) {
      TimelineResponse(
        reference = reference,
        prisonNumber = prisonNumber,
        events = events.map { timelineEventResourceMapper.fromDomainToModel(it) },
      )
    }
}
