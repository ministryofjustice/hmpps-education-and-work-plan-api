package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ExemptionProperties::class)
class ExemptionPropertiesConfiguration

@ConfigurationProperties(prefix = "exemption")
data class ExemptionProperties(
  private val extendDeadline: DeadlineExtensionRule,
) {
  val alwaysExtendDeadlines get() = extendDeadline == DeadlineExtensionRule.ALWAYS

  val onlyExtendDeadlinesWhenNotOverdue get() = extendDeadline == DeadlineExtensionRule.ONLY_WHEN_NOT_OVERDUE
}

enum class DeadlineExtensionRule {
  ALWAYS,
  ONLY_WHEN_NOT_OVERDUE,
}
