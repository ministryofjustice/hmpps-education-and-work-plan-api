package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.LocalDate

@ConfigurationProperties(prefix = "induction-extension")
data class InductionExtensionConfig(
  var periods: List<InductionExtensionPeriod> = emptyList(),
)

data class InductionExtensionPeriod(
  var start: LocalDate = LocalDate.MIN,
  var end: LocalDate = LocalDate.MIN,
)
