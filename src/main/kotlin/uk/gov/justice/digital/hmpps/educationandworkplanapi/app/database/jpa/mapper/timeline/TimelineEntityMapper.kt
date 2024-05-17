package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.timeline

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.domain.timeline.Timeline
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.TimelineEntity

@Mapper(
  uses = [
    TimelineEventEntityMapper::class,
  ],
)
interface TimelineEntityMapper {
  fun fromEntityToDomain(persisted: TimelineEntity): Timeline
}
