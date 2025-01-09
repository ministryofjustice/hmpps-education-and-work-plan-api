package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.inducton

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleDateCalculationService
import java.time.LocalDate

/**
 * Implementation of [InductionScheduleDateCalculationService] with implemented behaviours specific to the CIAG PES contracts.
 *
 * This bean is only enabled when the `ciag-kpi-processing-rule` property is set to `PES`; otherwise [PefInductionScheduleDateCalculationService]
 * is used.
 */
@Service
@ConditionalOnProperty(name = ["ciag-kpi-processing-rule"], havingValue = "PES")
class PesInductionScheduleDateCalculationService : InductionScheduleDateCalculationService() {

  /**
   * Returns a [CreateInductionScheduleDto] suitable for creating the specified prisoner's initial [InductionSchedule].
   *
   * Under the PES contract the prisoner's Induction cannot be scheduled until the prisoner's Screenings & Assessments
   * have been completed in Curious. The [InductionSchedule] is created with the status indicating it is waiting for
   * the Curious S&As, and an arbitrary deadline date of today.
   * Once the S&A's have been completed in Curious the [InductionSchedule] has it's status set to SCHEDULED and the
   * deadline date correctly set (via a listener on the event sent from Curious)
   */
  override fun determineCreateInductionScheduleDto(prisonNumber: String, admissionDate: LocalDate): CreateInductionScheduleDto =
    // TODO remove this hardcoded BXI
    CreateInductionScheduleDto(
      prisonNumber = prisonNumber,
      deadlineDate = LocalDate.now(),
      scheduleCalculationRule = InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
      scheduleStatus = InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS,
      prisonId = "BXI",
    )
}
