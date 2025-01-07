package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionPersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleEventService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.ActionPlanPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.CreateInitialReviewScheduleMapper

private val log = KotlinLogging.logger {}

@Service
class ReviewScheduleAdapter(
  private val inductionPersistenceAdapter: InductionPersistenceAdapter,
  private val actionPlanPersistenceAdapter: ActionPlanPersistenceAdapter,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val createInitialReviewScheduleMapper: CreateInitialReviewScheduleMapper,
  private val reviewScheduleService: ReviewScheduleService,
  private val reviewScheduleEventService: ReviewScheduleEventService,
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
    return reviewScheduleService.createInitialReviewSchedule(reviewScheduleDto)
      ?.also {
        reviewScheduleEventService.reviewScheduleCreated(it)
      }
  }
}
