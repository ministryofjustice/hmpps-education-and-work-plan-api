package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDate

data class InductionExtensionPeriod(
  val start: LocalDate,
  val end: LocalDate,
)

@Component
class InductionExtensionConfig(
  @param:Value("\${induction-extension.periods:}")
  private val periodsConfig: String,
) {

  val periods: List<InductionExtensionPeriod> = parsePeriods(periodsConfig)

  private fun parsePeriods(config: String): List<InductionExtensionPeriod> = config
    .split(',')
    .map { it.trim() }
    .filter { it.isNotEmpty() }
    .map { token ->
      val parts = token.split(':', limit = 2)
      require(parts.size == 2) { "Invalid period format: '$token' (expected start:end)" }
      val start = LocalDate.parse(parts[0].trim())
      val end = LocalDate.parse(parts[1].trim())
      InductionExtensionPeriod(start, end)
    }
}
