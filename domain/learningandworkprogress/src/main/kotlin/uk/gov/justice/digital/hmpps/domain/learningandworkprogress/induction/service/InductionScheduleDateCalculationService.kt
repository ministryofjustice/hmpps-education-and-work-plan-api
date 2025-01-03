package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import java.time.LocalDate

/**
 * Service class exposing methods that implement the business rules for calculating Induction Schedule dates.
 *
 * This class is deliberately final so that it cannot be subclassed, ensuring that the business rules stay within the
 * domain.
 */
class InductionScheduleDateCalculationService {
  companion object {
    private const val EXEMPTION_ADDITIONAL_DAYS = 5L
    private const val EXCLUSION_ADDITIONAL_DAYS = 10L
    private const val SYSTEM_OUTAGE_ADDITIONAL_DAYS = 5L
  }

  fun calculateAdjustedInductionDueDate(inductionSchedule: InductionSchedule): LocalDate =
    with(inductionSchedule) {
      val additionalDays = getExtensionDays(scheduleStatus)
      val todayPlusAdditionalDays = LocalDate.now().plusDays(additionalDays)
      maxOf(todayPlusAdditionalDays, deadlineDate)
    }

  private fun getExtensionDays(status: InductionScheduleStatus): Long =
    when {
      status.isExclusion -> EXCLUSION_ADDITIONAL_DAYS
      status.isExemption -> EXEMPTION_ADDITIONAL_DAYS
      status == InductionScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE -> SYSTEM_OUTAGE_ADDITIONAL_DAYS
      else -> 0 // Default case, if no condition matches
    }
}
