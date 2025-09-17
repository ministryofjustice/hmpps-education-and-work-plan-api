package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionPersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.ActionPlanPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.CreateInitialReviewScheduleMapper
import java.time.LocalDate

private val log = KotlinLogging.logger {}

@Service
class ScheduleAdapter(
  private val inductionPersistenceAdapter: InductionPersistenceAdapter,
  private val actionPlanPersistenceAdapter: ActionPlanPersistenceAdapter,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val createInitialReviewScheduleMapper: CreateInitialReviewScheduleMapper,
  private val reviewScheduleService: ReviewScheduleService,
  private val inductionScheduleService: InductionScheduleService,
) {

  fun requiresInductionSchedule(prisonNumber: String): Boolean {
    val inductionSchedule = runCatching {
      inductionScheduleService.getInductionScheduleForPrisoner(prisonNumber)
    }.getOrNull()
    return inductionSchedule == null && !isInductionComplete(prisonNumber)
  }

  fun isInductionComplete(prisonNumber: String): Boolean {
    val actionPlan = actionPlanPersistenceAdapter.getActionPlan(prisonNumber)
    return inductionPersistenceAdapter.getInduction(prisonNumber) != null &&
      actionPlan != null &&
      actionPlan.goals.isNotEmpty()
  }

  fun createInductionScheduleIfRequired(prisonNumber: String, prisonId: String = "N/A") {
    if (requiresInductionSchedule(prisonNumber)) {
      // Create the induction schedule for this person:
      inductionScheduleService.createInductionSchedule(
        prisonNumber = prisonNumber,
        prisonerAdmissionDate = LocalDate.now(),
        prisonId = prisonId,
      )
    }
  }

  fun completeInductionScheduleAndCreateInitialReviewSchedule(prisonNumber: String, prisonId: String = "N/A") {
    log.info { "Attempting to complete induction or create review schedule for $prisonNumber" }
    if (
      isInductionComplete(prisonNumber)
    ) {
      // COMPLETE the induction schedule IF it is not already completed.
      val inductionSchedule = runCatching {
        inductionScheduleService.getInductionScheduleForPrisoner(prisonNumber)
      }.getOrNull()
      if (inductionSchedule != null && inductionSchedule.scheduleStatus == InductionScheduleStatus.SCHEDULED) {
        inductionScheduleService.updateInductionSchedule(
          inductionSchedule = inductionSchedule,
          newStatus = InductionScheduleStatus.COMPLETED,
          prisonId = inductionSchedule.lastUpdatedAtPrison,
        )
      }

      val activeReviewSchedule =
        runCatching { reviewScheduleService.getActiveReviewScheduleForPrisoner(prisonNumber) }.getOrNull()

      if (activeReviewSchedule == null) {
        // Create initial review schedule
        val prisoner = prisonerSearchApiService.getPrisoner(prisonNumber)
        val reviewScheduleDto = createInitialReviewScheduleMapper.fromPrisonerToDomain(
          prisoner = prisoner,
          isReadmission = false,
          isTransfer = false,
        )
        reviewScheduleService.createInitialReviewSchedule(reviewScheduleDto)
      }
    }
  }
}
