package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper

import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@Component
class InstantMapper {
  fun toOffsetDateTime(instant: Instant?): OffsetDateTime? = instant?.atOffset(UTC)

  fun toInstant(offsetDateTime: OffsetDateTime?): Instant? = offsetDateTime?.toInstant()

  fun toInstant(localDate: LocalDate): Instant? = localDate.atStartOfDay(UTC).toInstant()
}
