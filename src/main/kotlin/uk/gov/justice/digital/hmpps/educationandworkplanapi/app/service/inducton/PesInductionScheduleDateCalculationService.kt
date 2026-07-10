package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.inducton

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
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
 * Implementation of [InductionScheduleDateCalculationService] with implemented behaviours specific to the CIAG PES contracts.
 *
 * This bean is only enabled when the `ciag-kpi-processing-rule` property is set to `PES`; otherwise [PefInductionScheduleDateCalculationService]
 * is used.
 */
@Service
@ConditionalOnProperty(name = ["ciag-kpi-processing-rule"], havingValue = "PES")
class PesInductionScheduleDateCalculationService(
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
   * Under the PES contract the prisoner's Induction cannot be scheduled until the prisoner's Screenings & Assessments
   * have been completed in Curious. The [InductionSchedule] is created with the status indicating it is waiting for
   * the Curious S&As, and an arbitrary deadline date of today.
   * Once the S&A's have been completed in Curious the [InductionSchedule] has it's status set to SCHEDULED and the
   * deadline date correctly set (via a listener on the event sent from Curious)
   */
  override fun determineCreateInductionScheduleDto(prisonNumber: String, admissionDate: LocalDate, prisonId: String): CreateInductionScheduleDto = CreateInductionScheduleDto(
    prisonNumber = prisonNumber,
    deadlineDate = LocalDate.now(clock),
    scheduleCalculationRule = inductionScheduleCalculationService.getCalculationRuleForNewPrisonAdmission(),
    scheduleStatus = InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS,
    prisonId = prisonId,
  )

  /**
   * Under the PES contract the Induction is due 10 days after the prisoner's Screening & Assessments are completed in
   * Curious. As Screening & Assessments cannot be completed in the future, the deadline is calculated from today,
   * meaning it can never fall in the past.
   */
  override fun determineDeadlineDateForCompletedAssessments(calculationRule: InductionScheduleCalculationRule): LocalDate = LocalDate.now(clock).plusDays(getNewAdmissionAdditionalDays(calculationRule))

  /**
   * PES implementation. When scheduling the Induction the deadline date should be today + 10 days, unless it is the
   * "extended deadline period" in which case it is + <TBC> days.
   */
  override fun getNewAdmissionAdditionalDays(calculationRule: InductionScheduleCalculationRule): Long = when (calculationRule) {
    InductionScheduleCalculationRule.NEW_PRISON_ADMISSION_EXTENDED_DEADLINE_PERIOD -> TEN_DAYS // TODO - update here if the business decide they want a longer deadline over extended deadline periods such as Christmas
    else -> TEN_DAYS
  }
}
