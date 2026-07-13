package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.inducton

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleDateCalculationService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleEventService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionSchedulePersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.EducationAssessmentEventRepository
import java.time.Clock
import java.time.LocalDate

/**
 * Implementation of [InductionScheduleService] with implemented behaviours specific to the CIAG PES contracts.
 *
 * This bean is only enabled when the `ciag-kpi-processing-rule` property is set to `PES`; otherwise [PefInductionScheduleService]
 * is used.
 */
@Service
@ConditionalOnProperty(name = ["ciag-kpi-processing-rule"], havingValue = "PES")
class PesInductionScheduleService(
  inductionSchedulePersistenceAdapter: InductionSchedulePersistenceAdapter,
  inductionScheduleEventService: InductionScheduleEventService,
  private val inductionScheduleDateCalculationService: InductionScheduleDateCalculationService,
  private val educationAssessmentEventRepository: EducationAssessmentEventRepository,
  private val clock: Clock,
) : InductionScheduleService(
  inductionSchedulePersistenceAdapter,
  inductionScheduleEventService,
  inductionScheduleDateCalculationService,
) {

  override fun updatedInductionDeadlineForProcessingTransfer(inductionSchedule: InductionSchedule): LocalDate = if (hasCompletedAllAssessments(inductionSchedule.prisonNumber)) {
    inductionScheduleDateCalculationService.calculateAdjustedInductionDueDate(inductionSchedule)
  } else {
    LocalDate.now(clock)
  }

  override fun updatedStatusForProcessingTransfer(inductionSchedule: InductionSchedule): InductionScheduleStatus = if (hasCompletedAllAssessments(inductionSchedule.prisonNumber)) {
    InductionScheduleStatus.SCHEDULED
  } else {
    InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS
  }

  private fun hasCompletedAllAssessments(prisonNumber: String): Boolean = educationAssessmentEventRepository.existsByPrisonNumberAndStatus(
    prisonNumber,
    EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
  )
}
