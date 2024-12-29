package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.timeline

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.timeline.Timeline
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.TimelineEntity

@Component
class TimelineEntityMapper(private val timelineEventEntityMapper: TimelineEventEntityMapper) {
  fun fromEntityToDomain(persisted: TimelineEntity): Timeline =
    with(persisted) {
      Timeline(
        reference = reference,
        prisonNumber = prisonNumber,
        events = events.map { timelineEventEntityMapper.fromEntityToDomain(it) },
      )
    }
}
