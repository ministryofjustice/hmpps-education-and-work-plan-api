package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionPersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.ActionPlanPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.CreateInitialReviewScheduleMapper

@Service
class ReviewScheduleService(
  private val inductionPersistenceAdapter: InductionPersistenceAdapter,
  private val actionPlanPersistenceAdapter: ActionPlanPersistenceAdapter,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val createInitialReviewScheduleMapper: CreateInitialReviewScheduleMapper,
  private val reviewService: ReviewService,
) {

  fun createInitialReviewScheduleIfInductionAndActionPlanExists(prisonNumber: String): ReviewSchedule? =
    if (
      inductionPersistenceAdapter.getInduction(prisonNumber) != null &&
      actionPlanPersistenceAdapter.getActionPlan(prisonNumber) != null
    ) {
      val prisoner = prisonerSearchApiService.getPrisoner(prisonNumber)
      val createInitialReviewScheduleDto = createInitialReviewScheduleMapper.fromPrisonerToDomain(
        prisoner = prisoner,
        isReadmission = false,
        isTransfer = false,
      )
      reviewService.createInitialReviewSchedule(createInitialReviewScheduleDto)
    } else {
      null
    }
}
