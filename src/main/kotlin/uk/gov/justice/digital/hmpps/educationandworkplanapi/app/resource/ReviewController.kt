package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.LegalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.CompletedActionPlanReviewResponseMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.CreateActionPlanReviewRequestMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.ScheduledActionPlanReviewResponseMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.PRISON_NUMBER_FORMAT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanReviewsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanReviewRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanReviewResponse

@RestController
@RequestMapping(value = ["/action-plans/{prisonNumber}/reviews"])
class ReviewController(
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val reviewService: ReviewService,
  private val scheduledActionPlanReviewResponseMapper: ScheduledActionPlanReviewResponseMapper,
  private val completedActionPlanReviewResponseMapper: CompletedActionPlanReviewResponseMapper,
  private val createActionPlanReviewRequestMapper: CreateActionPlanReviewRequestMapper,
) {

  @GetMapping
  @PreAuthorize(HAS_VIEW_REVIEWS)
  fun getActionPlanReviews(
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
  ): ActionPlanReviewsResponse {
    val latestReviewSchedule = reviewService.getLatestReviewScheduleForPrisoner(prisonNumber)
    val completedReviews = reviewService.getCompletedReviewsForPrisoner(prisonNumber)
    return ActionPlanReviewsResponse(
      latestReviewSchedule = scheduledActionPlanReviewResponseMapper.fromDomainToModel(latestReviewSchedule),
      completedReviews = completedReviews.map { completedActionPlanReviewResponseMapper.fromDomainToModel(it) },
    )
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @Transactional
  fun createActionPlanReview(
    @Valid
    @RequestBody createActionPlanReviewRequest: CreateActionPlanReviewRequest,
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
  ): CreateActionPlanReviewResponse {
    val prisoner = prisonerSearchApiService.getPrisoner(prisonNumber)
    val prisonerReleaseDate = prisoner.releaseDate
    val prisonerSentenceType = toSentenceType(prisoner.legalStatus)

    val completedReview = reviewService.createReview(
      createActionPlanReviewRequestMapper.fromModelToDomain(
        prisonNumber = prisonNumber,
        releaseDate = prisonerReleaseDate,
        sentenceType = prisonerSentenceType,
        request = createActionPlanReviewRequest,
      ),
    )

    return CreateActionPlanReviewResponse(
      wasLastReviewBeforeRelease = completedReview.wasLastReviewBeforeRelease,
      latestReviewSchedule = scheduledActionPlanReviewResponseMapper.fromDomainToModel(completedReview.latestReviewSchedule),
    )
  }

  private fun toSentenceType(legalStatus: LegalStatus): SentenceType =
    when (legalStatus) {
      LegalStatus.RECALL -> SentenceType.RECALL
      LegalStatus.DEAD -> SentenceType.DEAD
      LegalStatus.INDETERMINATE_SENTENCE -> SentenceType.INDETERMINATE_SENTENCE
      LegalStatus.SENTENCED -> SentenceType.SENTENCED
      LegalStatus.CONVICTED_UNSENTENCED -> SentenceType.CONVICTED_UNSENTENCED
      LegalStatus.CIVIL_PRISONER -> SentenceType.CIVIL_PRISONER
      LegalStatus.IMMIGRATION_DETAINEE -> SentenceType.IMMIGRATION_DETAINEE
      LegalStatus.REMAND -> SentenceType.REMAND
      LegalStatus.UNKNOWN -> SentenceType.UNKNOWN
      LegalStatus.OTHER -> SentenceType.OTHER
    }
}
