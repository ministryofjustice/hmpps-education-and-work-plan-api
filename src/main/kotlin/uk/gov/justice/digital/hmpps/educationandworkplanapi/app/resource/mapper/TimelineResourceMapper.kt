package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.Timeline
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineResponse

@Mapper(
  uses = [
    TimelineEventResourceMapper::class,
  ],
)
interface TimelineResourceMapper {
  fun fromDomainToModel(timeline: Timeline): TimelineResponse
}
