package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.inducton

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleDateCalculationService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleEventService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionSchedulePersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import java.time.LocalDate

/**
 * Implementation of [InductionScheduleService] with implemented behaviours specific to the CIAG PEF contracts.
 *
 * This bean is enabled when the PES implementation bean ([PesInductionScheduleService]) is not present.
 */
@Service
@Primary
@ConditionalOnMissingBean(PesInductionScheduleService::class)
class PefInductionScheduleService(
  inductionSchedulePersistenceAdapter: InductionSchedulePersistenceAdapter,
  inductionScheduleEventService: InductionScheduleEventService,
  private val inductionScheduleDateCalculationService: InductionScheduleDateCalculationService,
) : InductionScheduleService(
  inductionSchedulePersistenceAdapter,
  inductionScheduleEventService,
  inductionScheduleDateCalculationService,
) {

  override fun updatedInductionDeadlineForProcessingTransfer(inductionSchedule: InductionSchedule): LocalDate = inductionScheduleDateCalculationService.calculateAdjustedInductionDueDate(inductionSchedule)

  override fun updatedStatusForProcessingTransfer(inductionSchedule: InductionSchedule): InductionScheduleStatus = InductionScheduleStatus.SCHEDULED
}
