package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.constraints.Pattern
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.CompletedActionPlanReviewResponseMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.ScheduledActionPlanReviewResponseMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.PRISON_NUMBER_FORMAT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanReviewsResponse

@RestController
@RequestMapping(value = ["/action-plans/{prisonNumber}/reviews"])
class ReviewController(
  private val reviewService: ReviewService,
  private val scheduledActionPlanReviewResponseMapper: ScheduledActionPlanReviewResponseMapper,
  private val completedActionPlanReviewResponseMapper: CompletedActionPlanReviewResponseMapper,
) {

  @GetMapping
  @PreAuthorize(HAS_VIEW_REVIEWS)
  fun getActionPlanReviews(
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
  ): ActionPlanReviewsResponse {
    val scheduledReview = reviewService.getReviewScheduleForPrisoner(prisonNumber)
    val completedReviews = reviewService.getCompletedReviewsForPrisoner(prisonNumber)
    return ActionPlanReviewsResponse(
      scheduledReview = scheduledActionPlanReviewResponseMapper.fromDomainToModel(scheduledReview),
      completedReviews = completedReviews.map { completedActionPlanReviewResponseMapper.fromDomainToModel(it) },
    )
  }
}
