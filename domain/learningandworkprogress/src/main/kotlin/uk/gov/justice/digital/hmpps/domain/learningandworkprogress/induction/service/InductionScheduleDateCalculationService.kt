package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionScheduleDto
import java.time.LocalDate

/**
 * Abstract service class exposing methods that implement the business rules for calculating Induction Schedule dates.
 *
 * This implemented methods are deliberately final so that they cannot be subclassed, ensuring that the business rules stay within the
 * domain.
 * The only exception to this are the abstract methods which are intended to be implemented by subclasses.
 */
abstract class InductionScheduleDateCalculationService(
  private val scheduleDateNotBefore: LocalDate? = null,
) {
  companion object {
    private const val EXEMPTION_ADDITIONAL_DAYS = 5L
    private const val EXCLUSION_ADDITIONAL_DAYS = 10L
    private const val TWENTY_DAYS = 20L
    private const val SYSTEM_OUTAGE_ADDITIONAL_DAYS = 5L
  }

  /**
   * Returns a [CreateInductionScheduleDto] suitable for creating the specified prisoner's initial [InductionSchedule].
   * Known implementations at this time are those for the CIAG PEF and PES contracts, where a prisoner's initial Induction Schedule
   * is created differently under those contracts.
   */
  abstract fun determineCreateInductionScheduleDto(prisonNumber: String, admissionDate: LocalDate, prisonId: String, newAdmission: Boolean = true, releaseDate: LocalDate? = null): CreateInductionScheduleDto

  fun calculateAdjustedInductionDueDate(inductionSchedule: InductionSchedule): LocalDate = with(inductionSchedule) {
    if (inductionSchedule.scheduleStatus == InductionScheduleStatus.EXEMPT_PRISONER_TRANSFER) {
      return baseScheduleDate().plusDays(TWENTY_DAYS)
    }
    val additionalDays = getExtensionDays(scheduleStatus)
    val baseDatePlusAdditionalDays = baseScheduleDate().plusDays(additionalDays)
    maxOf(baseDatePlusAdditionalDays, deadlineDate)
  }

  private fun getExtensionDays(status: InductionScheduleStatus): Long = when {
    status.isExclusion -> EXCLUSION_ADDITIONAL_DAYS
    status.isExemption -> EXEMPTION_ADDITIONAL_DAYS
    status == InductionScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE -> SYSTEM_OUTAGE_ADDITIONAL_DAYS
    else -> 0 // Default case, if no condition matches
  }

  /**
   * Returns the base date from which all Induction Schedule dates are calculated
   */
  protected fun baseScheduleDate(): LocalDate {
    val today = LocalDate.now()
    return if (scheduleDateNotBefore != null && scheduleDateNotBefore.isAfter(today)) {
      scheduleDateNotBefore
    } else {
      today
    }
  }
}
