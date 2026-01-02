package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

@Configuration
@EnableConfigurationProperties(PrisonEducationServiceProperties::class)
class PrisonEducationServiceConfiguration

@ConfigurationProperties(prefix = "pes")
data class PrisonEducationServiceProperties(
  @param:DateTimeFormat(iso = DateTimeFormat.ISO.DATE) val contractStartDate: LocalDate,
)
