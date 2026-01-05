package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

@Configuration
class ClockConfig(
  @param:Value("\${app.clock.fixed-date:}") private val fixedDate: String?,
) {

  @Bean
  fun clock(): Clock = if (fixedDate.isNullOrBlank()) {
    Clock.systemDefaultZone()
  } else {
    val date = LocalDate.parse(fixedDate)
    val zone = ZoneId.systemDefault()
    Clock.fixed(date.atStartOfDay(zone).toInstant(), zone)
  }
}
