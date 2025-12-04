package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.inducton

import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleDateCalculationService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.InductionExtensionConfig
import java.time.LocalDate

private val log = KotlinLogging.logger {}

/**
 * Implementation of [InductionScheduleDateCalculationService] with implemented behaviours specific to the CIAG PEF contracts.
 *
 * This bean is enabled when the PES implementation bean ([PesInductionScheduleDateCalculationService]) is not present.
 */
@Service
@Primary
@ConditionalOnMissingBean(PesInductionScheduleDateCalculationService::class)
class PefInductionScheduleDateCalculationService(private val inductionExtensionConfig: InductionExtensionConfig) : InductionScheduleDateCalculationService() {

  companion object {
    private const val DAYS_AFTER_ADMISSION = 20L
    private const val DAYS_AFTER_ADMISSION_EXTENDED = 25L
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
    dataCorrection: Boolean,
  ): CreateInductionScheduleDto = if (dataCorrection) {
    CreateInductionScheduleDto(
      prisonNumber = prisonNumber,
      deadlineDate = calculateDeadlineDate(releaseDate),
      scheduleCalculationRule = InductionScheduleCalculationRule.EXISTING_PRISONER,
      scheduleStatus = InductionScheduleStatus.SCHEDULED,
      prisonId = prisonId,
    )
  } else if (newAdmission) {
    val calculationRule = getNewAdmissionCalculationRule()
    CreateInductionScheduleDto(
      prisonNumber = prisonNumber,
      deadlineDate = latestOf(admissionDate, LocalDate.now()).plusDays(getNewAdmissionAdditionalDays(calculationRule)),
      scheduleCalculationRule = calculationRule,
      scheduleStatus = InductionScheduleStatus.SCHEDULED,
      prisonId = prisonId,
    )
  } else {
    CreateInductionScheduleDto(
      prisonNumber = prisonNumber,
      deadlineDate = dataCorrectionDeadlineDate(releaseDate),
      scheduleCalculationRule = InductionScheduleCalculationRule.EXISTING_PRISONER,
      scheduleStatus = InductionScheduleStatus.SCHEDULED,
      prisonId = prisonId,
    )
  }

  private fun getNewAdmissionAdditionalDays(calculationRule: InductionScheduleCalculationRule): Long = when (calculationRule) {
    InductionScheduleCalculationRule.NEW_PRISON_ADMISSION_EXTENDED_DEADLINE_PERIOD -> {
      DAYS_AFTER_ADMISSION_EXTENDED
    }
    else -> DAYS_AFTER_ADMISSION
  }

  // This is to check whether today's date is during a period when the deadline is extended.
  // e.g. during a special holiday like Christmas
  fun getNewAdmissionCalculationRule(): InductionScheduleCalculationRule {
    val today = LocalDate.now()

    val inHolidayPeriod = inductionExtensionConfig.periods.any { period ->
      !today.isBefore(period.start) && !today.isAfter(period.end)
    }

    log.debug("today: {}, inHolidayPeriod: {}", today, inHolidayPeriod)
    return if (inHolidayPeriod) {
      InductionScheduleCalculationRule.NEW_PRISON_ADMISSION_EXTENDED_DEADLINE_PERIOD
    } else {
      InductionScheduleCalculationRule.NEW_PRISON_ADMISSION
    }
  }

  private fun dataCorrectionDeadlineDate(releaseDate: LocalDate?): LocalDate {
    val defaultDeadlineDate = LocalDate.of(2025, 10, 1)
    if (releaseDate != null) {
      val sevenDaysBeforeRelease = releaseDate.minusDays(7)
      if (sevenDaysBeforeRelease.isBefore(defaultDeadlineDate)) {
        return sevenDaysBeforeRelease
      }
    }
    return defaultDeadlineDate
  }

  private fun calculateDeadlineDate(releaseDate: LocalDate?): LocalDate {
    val sixMonthsAfterBase = baseScheduleDate().plusMonths(6)

    return when {
      releaseDate == null -> sixMonthsAfterBase
      releaseDate.isBefore(sixMonthsAfterBase) -> releaseDate.minusDays(7)
      else -> sixMonthsAfterBase
    }
  }

  private fun latestOf(admissionDate: LocalDate, scheduleDateNotBefore: LocalDate?): LocalDate = if (scheduleDateNotBefore != null && scheduleDateNotBefore.isAfter(admissionDate)) {
    scheduleDateNotBefore
  } else {
    admissionDate
  }
}
