package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

@Configuration
class ClockConfig(
  @param:Value("\${app.clock.fixed-date-time:}") private val fixedDateTime: String?,
) {

  @Bean
  @Primary
  fun testClock(): Clock = if (fixedDateTime.isNullOrBlank()) {
    Clock.systemDefaultZone()
  } else {
    val dateTime = LocalDateTime.parse(fixedDateTime)
    val zone = ZoneId.systemDefault()
    Clock.fixed(dateTime.atZone(zone).toInstant(), zone)
  }
}
