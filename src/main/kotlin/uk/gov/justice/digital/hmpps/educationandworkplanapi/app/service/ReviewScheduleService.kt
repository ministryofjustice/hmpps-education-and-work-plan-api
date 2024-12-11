package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.AsyncResult
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionPersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.ActionPlanPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.CreateInitialReviewScheduleMapper
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

@Service
class ReviewScheduleService(
  private val inductionPersistenceAdapter: InductionPersistenceAdapter,
  private val actionPlanPersistenceAdapter: ActionPlanPersistenceAdapter,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val createInitialReviewScheduleMapper: CreateInitialReviewScheduleMapper,
  private val reviewService: ReviewService,
) {

  @Async
  fun createInitialReviewScheduleIfInductionAndActionPlanExists(prisonNumber: String): Future<ReviewSchedule?> {
    val inductionExists = inductionPersistenceAdapter.getInduction(prisonNumber) != null
    val actionPlanExists = actionPlanPersistenceAdapter.getActionPlan(prisonNumber) != null

    return if (inductionExists && actionPlanExists) {
      val prisoner = prisonerSearchApiService.getPrisoner(prisonNumber)
      val createInitialReviewScheduleDto = createInitialReviewScheduleMapper.fromPrisonerToDomain(
        prisoner = prisoner,
        isReadmission = false,
        isTransfer = false,
      )
      CompletableFuture.completedFuture(reviewService.createInitialReviewSchedule(createInitialReviewScheduleDto))
    } else {
      CompletableFuture.completedFuture(null)
    }
  }
}
