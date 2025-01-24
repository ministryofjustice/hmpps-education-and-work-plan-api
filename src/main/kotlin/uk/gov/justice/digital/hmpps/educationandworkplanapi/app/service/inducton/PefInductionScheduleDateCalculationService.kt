package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.inducton

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleDateCalculationService
import java.time.LocalDate

/**
 * Implementation of [InductionScheduleDateCalculationService] with implemented behaviours specific to the CIAG PEF contracts.
 *
 * This bean is enabled when the PES implementation bean ([PesInductionScheduleDateCalculationService]) is not present.
 */
@Service
@Primary
@ConditionalOnMissingBean(PesInductionScheduleDateCalculationService::class)
class PefInductionScheduleDateCalculationService : InductionScheduleDateCalculationService() {

  companion object {
    private const val DAYS_AFTER_ADMISSION = 20L
  }

  /**
   * Returns a [CreateInductionScheduleDto] suitable for creating the specified prisoner's initial [InductionSchedule].
   *
   * Under the PEF contract the prisoner's Induction is immediately scheduled with a deadline date of the prisoner's admission date
   * plus 20 days.
   */
  override fun determineCreateInductionScheduleDto(
    prisonNumber: String,
    admissionDate: LocalDate,
    prisonId: String,
    newAdmission: Boolean,
  ): CreateInductionScheduleDto {
    return if (newAdmission) {
      CreateInductionScheduleDto(
        prisonNumber = prisonNumber,
        deadlineDate = admissionDate.plusDays(DAYS_AFTER_ADMISSION),
        scheduleCalculationRule = InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
        scheduleStatus = InductionScheduleStatus.SCHEDULED,
        prisonId = prisonId,
      )
    } else {
      CreateInductionScheduleDto(
        prisonNumber = prisonNumber,
        deadlineDate = getDeadlineDate(),
        scheduleCalculationRule = InductionScheduleCalculationRule.EXISTING_PRISONER,
        scheduleStatus = InductionScheduleStatus.SCHEDULED,
        prisonId = prisonId,
      )
    }
  }

  // This will return the grater of today's date plus six months or 1/04/2025 plus six months
  // This should only be used by the ETL process, which will take place before 31/03/2025.
  fun getDeadlineDate(): LocalDate {
    val todayPlus6Months = LocalDate.now().plusMonths(6)

    // Hardcoded date: 1st April 2025
    val aprilFirst2025 = LocalDate.of(2025, 4, 1)
    val aprilFirstPlus6Months = aprilFirst2025.plusMonths(6)

    // Return the later of the two dates
    return if (todayPlus6Months.isAfter(aprilFirstPlus6Months)) {
      todayPlus6Months
    } else {
      aprilFirstPlus6Months
    }
  }
}
