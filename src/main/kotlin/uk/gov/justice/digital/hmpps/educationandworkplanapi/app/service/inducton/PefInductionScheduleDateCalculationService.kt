package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.inducton

import org.springframework.beans.factory.annotation.Value
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
class PefInductionScheduleDateCalculationService(
  @Value("\${EDUCATION_CONTRACTS_START_DATE:}") private val scheduleDateNotBefore: LocalDate? = null,
) : InductionScheduleDateCalculationService(scheduleDateNotBefore) {

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
    releaseDate: LocalDate?,
  ): CreateInductionScheduleDto {
    return if (newAdmission) {
      CreateInductionScheduleDto(
        prisonNumber = prisonNumber,
        deadlineDate = latestOf(admissionDate, scheduleDateNotBefore).plusDays(DAYS_AFTER_ADMISSION),
        scheduleCalculationRule = InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
        scheduleStatus = InductionScheduleStatus.SCHEDULED,
        prisonId = prisonId,
      )
    } else {
      CreateInductionScheduleDto(
        prisonNumber = prisonNumber,
        deadlineDate = calculateDeadlineDate(releaseDate),
        scheduleCalculationRule = InductionScheduleCalculationRule.EXISTING_PRISONER,
        scheduleStatus = InductionScheduleStatus.SCHEDULED,
        prisonId = prisonId,
      )
    }
  }

  private fun calculateDeadlineDate(releaseDate: LocalDate?): LocalDate {
    val sixMonthsAfterBase = baseScheduleDate().plusMonths(6)

    return when {
      releaseDate == null -> sixMonthsAfterBase
      releaseDate.isBefore(sixMonthsAfterBase) -> releaseDate.minusDays(7)
      else -> sixMonthsAfterBase
    }
  }

  private fun latestOf(admissionDate: LocalDate, scheduleDateNotBefore: LocalDate?): LocalDate =
    if (scheduleDateNotBefore != null && scheduleDateNotBefore.isAfter(admissionDate)) {
      scheduleDateNotBefore
    } else {
      admissionDate
    }
}
