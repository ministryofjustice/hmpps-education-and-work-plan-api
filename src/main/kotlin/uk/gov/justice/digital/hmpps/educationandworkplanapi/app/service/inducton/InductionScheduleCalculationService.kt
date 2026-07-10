package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.inducton

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.InductionExtensionConfig
import java.time.Clock
import java.time.LocalDate

private val log = KotlinLogging.logger {}

@Service
class InductionScheduleCalculationService(
  private val inductionExtensionConfig: InductionExtensionConfig,
  private val clock: Clock,
) {

  /**
   * Returns an [InductionScheduleCalculationRule] for a new prison admission based on whether the deadline is to
   * be extended during special holiday periods such as Christmas
   */
  fun getCalculationRuleForNewPrisonAdmission(): InductionScheduleCalculationRule {
    val today = LocalDate.now(clock)

    val inHolidayPeriod = inductionExtensionConfig.periods.any { period ->
      !today.isBefore(period.start) && !today.isAfter(period.end)
    }

    log.debug("Holiday periods: {}", inductionExtensionConfig.periods)
    log.debug("today: {}, inHolidayPeriod: {}", today, inHolidayPeriod)
    return if (inHolidayPeriod) {
      InductionScheduleCalculationRule.NEW_PRISON_ADMISSION_EXTENDED_DEADLINE_PERIOD
    } else {
      InductionScheduleCalculationRule.NEW_PRISON_ADMISSION
    }
  }
}
