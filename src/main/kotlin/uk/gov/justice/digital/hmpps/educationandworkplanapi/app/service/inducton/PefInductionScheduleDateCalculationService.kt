package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.inducton

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleDateCalculationService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionSchedulePropertiesProvider
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.ExemptionProperties
import java.time.Clock
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
  private val clock: Clock,
  private val inductionScheduleCalculationService: InductionScheduleCalculationService,
  exemptionProperties: ExemptionProperties,
) : InductionScheduleDateCalculationService(
  clock = clock,
  propertiesProvider = object : InductionSchedulePropertiesProvider {
    override val onlyExtendDeadlinesWhenNotOverdue: Boolean
      get() = exemptionProperties.onlyExtendDeadlinesWhenNotOverdue
  },
) {

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
  ): CreateInductionScheduleDto {
    val calculationRule = inductionScheduleCalculationService.getCalculationRuleForNewPrisonAdmission()
    return CreateInductionScheduleDto(
      prisonNumber = prisonNumber,
      deadlineDate = latestOf(admissionDate, LocalDate.now(clock)).plusDays(getNewAdmissionAdditionalDays(calculationRule)),
      scheduleCalculationRule = calculationRule,
      scheduleStatus = InductionScheduleStatus.SCHEDULED,
      prisonId = prisonId,
    )
  }

  /**
   * PEF implementation. When scheduling the Induction the deadline date should be today + 20 days, unless it is the
   * "extended deadline period" in which case it is + 25 days.
   */
  override fun getNewAdmissionAdditionalDays(calculationRule: InductionScheduleCalculationRule): Long = when (calculationRule) {
    InductionScheduleCalculationRule.NEW_PRISON_ADMISSION_EXTENDED_DEADLINE_PERIOD -> TWENTY_FIVE_DAYS
    else -> TWENTY_DAYS
  }

  /**
   * PEF implementation. Induction Schedules are extended by 20 days when a prisoner is transferred
   */
  override fun extensionDaysForTransfer(): Long = TWENTY_DAYS

  /**
   * Not supported under the PEF contract: a prisoner's Induction is scheduled on prison admission, not following the
   * completion of their Screening & Assessments, so there is no S&A based deadline to calculate.
   */
  override fun determineDeadlineDateForCompletedAssessments(calculationRule: InductionScheduleCalculationRule): LocalDate = throw UnsupportedOperationException(
    "Induction Schedules are not scheduled following Screening & Assessment completion under the PEF contract",
  )

  private fun latestOf(admissionDate: LocalDate, scheduleDateNotBefore: LocalDate?): LocalDate = if (scheduleDateNotBefore != null && scheduleDateNotBefore.isAfter(admissionDate)) {
    scheduleDateNotBefore
  } else {
    admissionDate
  }
}
