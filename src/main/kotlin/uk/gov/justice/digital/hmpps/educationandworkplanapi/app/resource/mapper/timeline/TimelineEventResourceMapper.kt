package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.timeline

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventResponse
import java.time.Instant
import java.util.UUID

@Mapper(
  uses = [
    InstantMapper::class,
  ],
  imports = [
    Instant::class,
    UUID::class,
  ],
)
interface TimelineEventResourceMapper {
  fun fromDomainToModel(timelineEventDomain: TimelineEvent): TimelineEventResponse
}
