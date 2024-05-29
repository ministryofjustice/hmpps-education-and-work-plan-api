package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.timeline

import org.mapstruct.Mapper
import org.mapstruct.Mapping
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
abstract class TimelineEventResourceMapper {
  @Mapping(target = "contextualInfo", expression = "java( buildContextualInfo(timelineEventDomain) )")
  abstract fun fromDomainToModel(timelineEventDomain: TimelineEvent): TimelineEventResponse

  protected fun buildContextualInfo(timelineEventDomain: TimelineEvent): Map<String, String> =
    timelineEventDomain.contextualInfo?.mapKeys { it.key.toString() } ?: emptyMap()
}
