package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import java.time.Clock
import java.time.Instant
import java.util.Optional

@Configuration
class ClockConfiguration {

  @Bean
  fun clock(): Clock = Clock.systemDefaultZone()

  @Bean
  fun dateTimeProvider(clock: Clock): DateTimeProvider = DateTimeProvider { Optional.of(Instant.now(clock)) }
}
