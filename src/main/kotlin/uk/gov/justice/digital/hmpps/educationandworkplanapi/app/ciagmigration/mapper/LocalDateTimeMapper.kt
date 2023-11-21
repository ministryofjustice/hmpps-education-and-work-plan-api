package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.mapper

import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC

@Component
class LocalDateTimeMapper {
  fun toInstant(localDateTime: LocalDateTime?): Instant? = localDateTime?.toInstant(UTC)
}
