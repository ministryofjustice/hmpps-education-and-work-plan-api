package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.timeline

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.TimelineEventEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent

@Mapper
interface TimelineEventEntityMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  fun fromDomainToEntity(timelineEvent: TimelineEvent): TimelineEventEntity

  fun fromEntityToDomain(persisted: TimelineEventEntity): TimelineEvent
}
