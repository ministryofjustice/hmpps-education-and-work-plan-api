package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionPersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.ActionPlanPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventPublisher
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.CreateInitialReviewScheduleMapper

@Service
class ReviewScheduleAdapter(
  private val inductionPersistenceAdapter: InductionPersistenceAdapter,
  private val actionPlanPersistenceAdapter: ActionPlanPersistenceAdapter,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val createInitialReviewScheduleMapper: CreateInitialReviewScheduleMapper,
  private val reviewService: ReviewService,
  private val eventPublisher: EventPublisher,
) {

  fun createInitialReviewScheduleIfInductionAndActionPlanExists(prisonNumber: String): ReviewSchedule? {
    // validate
    if (
      inductionPersistenceAdapter.getInduction(prisonNumber) == null ||
      actionPlanPersistenceAdapter.getActionPlan(prisonNumber) == null
    ) {
      return null
    }

    // Create initial review schedule
    val prisoner = prisonerSearchApiService.getPrisoner(prisonNumber)
    val reviewScheduleDto = createInitialReviewScheduleMapper.fromPrisonerToDomain(
      prisoner = prisoner,
      isReadmission = false,
      isTransfer = false,
    )
    val reviewSchedule = reviewService.createInitialReviewSchedule(reviewScheduleDto)

    followOnEvents(prisonNumber)

    return reviewSchedule
  }

  private fun followOnEvents(prisonNumber: String) {
    // TODO telemetry etc
    eventPublisher.createAndPublishReviewScheduleEvent(prisonNumber)
  }
}
