package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionScheduleDto
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

/**
 * Abstract service class exposing methods that implement the business rules for calculating Induction Schedule dates.
 *
 * This implemented methods are deliberately final so that they cannot be subclassed, ensuring that the business rules stay within the
 * domain.
 * The only exception to this are the abstract methods which are intended to be implemented by subclasses.
 */
abstract class InductionScheduleDateCalculationService(
  private val clock: Clock,
  private val scheduleDateNotBefore: LocalDate? = null,
  private val propertiesProvider: InductionSchedulePropertiesProvider,
) {
  companion object {
    const val FIVE_DAYS = 5L
    const val TEN_DAYS = 10L
    const val TWENTY_DAYS = 20L
    const val TWENTY_FIVE_DAYS = 25L
  }

  /**
   * Returns a [CreateInductionScheduleDto] suitable for creating the specified prisoner's initial [InductionSchedule].
   * Known implementations at this time are those for the CIAG PEF and PES contracts, where a prisoner's initial Induction Schedule
   * is created differently under those contracts.
   */
  abstract fun determineCreateInductionScheduleDto(prisonNumber: String, admissionDate: LocalDate, prisonId: String): CreateInductionScheduleDto

  /**
   * Returns the deadline date for a prisoner's Induction once their Screening & Assessments have been completed in Curious.
   *
   * Only applicable under contracts where the Induction is scheduled following completion of the prisoner's S&As (the PES
   * contract); implementations for contracts that do not schedule Inductions this way are not expected to support it.
   */
  abstract fun determineDeadlineDateForCompletedAssessments(calculationRule: InductionScheduleCalculationRule): LocalDate

  /**
   * Returns the number of days that are added to the Induction deadline date when processing a transfer
   */
  abstract fun extensionDaysForTransfer(): Long

  fun calculateAdjustedInductionDueDate(inductionSchedule: InductionSchedule): LocalDate = with(inductionSchedule) {
    if (propertiesProvider.onlyExtendDeadlinesWhenNotOverdue && hadUserAppliedExemptionWhenInductionAlreadyOverdue()) {
      deadlineDate
    } else {
      if (scheduleStatus == InductionScheduleStatus.EXEMPT_PRISONER_TRANSFER) {
        return baseScheduleDate().plusDays(extensionDaysForTransfer())
      }
      val additionalDays = getExtensionDays(scheduleStatus)
      val baseDatePlusAdditionalDays = baseScheduleDate().plusDays(additionalDays)
      maxOf(baseDatePlusAdditionalDays, deadlineDate)
    }
  }

  abstract fun getNewAdmissionAdditionalDays(calculationRule: InductionScheduleCalculationRule): Long

  private fun getExtensionDays(status: InductionScheduleStatus): Long = when {
    status == InductionScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE -> FIVE_DAYS
    status.isExclusion -> TEN_DAYS
    status.isExemption -> FIVE_DAYS
    else -> 0 // Default case, if no condition matches
  }

  /**
   * Returns the base date from which all Induction Schedule dates are calculated
   */
  private fun baseScheduleDate(): LocalDate {
    val today = LocalDate.now(clock)
    return if (scheduleDateNotBefore != null && scheduleDateNotBefore.isAfter(today)) {
      scheduleDateNotBefore
    } else {
      today
    }
  }

  private fun InductionSchedule.hadUserAppliedExemptionWhenInductionAlreadyOverdue(): Boolean = (scheduleStatus == InductionScheduleStatus.EXEMPT_TEMP_ABSENCE || scheduleStatus.canBeSetByUserAction) &&
    deadlineDate.isBefore(LocalDate.ofInstant(lastUpdatedAt, ZoneId.systemDefault()))
}

interface InductionSchedulePropertiesProvider {
  val onlyExtendDeadlinesWhenNotOverdue: Boolean
}
