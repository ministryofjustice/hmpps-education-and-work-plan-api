package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.TimelineEventEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline.TimelineEvent

@Mapper
interface TimelineEventEntityMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "createdAtPrison", source = "prisonId")
  fun fromDomainToEntity(timelineEvent: TimelineEvent): TimelineEventEntity

  @Mapping(target = "prisonId", source = "createdAtPrison")
  fun fromEntityToDomain(persisted: TimelineEventEntity): TimelineEvent
}
