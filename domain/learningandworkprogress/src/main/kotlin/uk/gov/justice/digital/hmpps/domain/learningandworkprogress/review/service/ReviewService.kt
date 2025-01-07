package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.CompletedReview
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNoReleaseDateForSentenceTypeException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CompletedReviewDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateCompletedReviewDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.UpdateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.effectiveSentenceType

private val log = KotlinLogging.logger {}

/**
 * Service class exposing methods that implement the business rules for the Review domain.
 *
 * Applications using Reviews and ReviewSchedules must new up an instance of this class providing implementations of
 * [ReviewEventService], [ReviewPersistenceAdapter] and [ReviewSchedulePersistenceAdapter].
 *
 * This class is deliberately final so that it cannot be subclassed, ensuring that the business rules stay within the
 * domain.
 */
class ReviewService(
  private val reviewEventService: ReviewEventService,
  private val reviewPersistenceAdapter: ReviewPersistenceAdapter,
  private val reviewSchedulePersistenceAdapter: ReviewSchedulePersistenceAdapter,
  private val reviewScheduleService: ReviewScheduleService,
) {

  private val reviewScheduleDateCalculationService = ReviewScheduleDateCalculationService()

  /**
   * Returns a list of all [CompletedReview]s for the prisoner identified by their prison number. An empty list is
   * returned if the prisoner has no Completed Reviews.
   */
  fun getCompletedReviewsForPrisoner(prisonNumber: String): List<CompletedReview> =
    reviewPersistenceAdapter.getCompletedReviews(prisonNumber)

  /**
   * Creates a [CompletedReview] for the prisoner. and updates their current [ReviewSchedule] to be COMPLETED.
   *
   * Returns a [CompletedReviewDto] containing the completed review, a flag indicating whether this was the prisoner's
   * last review or not, and the prisoners latest [ReviewSchedule].
   *
   * If this was the prisoner's last review before release a new [ReviewSchedule] is not created, and their latest
   * [ReviewSchedule] is the one that was updated to be COMPLETED.
   * If this is not the prisoner's last review and a new Review Schedule Window is calculated for them, a new [ReviewSchedule]
   * is created with a status of SCHEDULED. This therefore becomes their latest Review Schedule.
   *
   * If the prisoner does not already have an active [ReviewSchedule] a [ReviewScheduleNotFoundException] is thrown.
   *
   * To create a new [ReviewSchedule] the prisoner is required to have a release date for most sentence types. The
   * exception to this are sentence types REMAND, CONVICTED_UNSENTENCED and INDETERMINATE_SENTENCE. All other sentence
   * types require a release date. Throws a [ReviewScheduleNoReleaseDateForSentenceTypeException] if this condition
   * is not satisfied.
   */
  fun createReview(createCompletedReviewDto: CreateCompletedReviewDto): CompletedReviewDto =
    with(createCompletedReviewDto) {
      val sentenceType = effectiveSentenceType(prisonerSentenceType, prisonerHasIndeterminateFlag, prisonerHasRecallFlag)

      // Get the active review schedule
      val currentReviewSchedule = reviewScheduleService.getActiveReviewScheduleForPrisoner(prisonNumber)

      // Persist a new CompletedReview
      val completedReview = reviewPersistenceAdapter.createCompletedReview(
        createCompletedReviewDto = this,
        reviewSchedule = currentReviewSchedule,
      )

      // Update the current review schedule to mark it as Completed
      val completedReviewSchedule = reviewSchedulePersistenceAdapter.updateReviewSchedule(
        UpdateReviewScheduleDto.setStatusToCompletedAtPrison(currentReviewSchedule, prisonId),
      )

      // Work out the next review schedule calculation rule and schedule window
      val releaseDate = prisonerReleaseDate
      val reviewScheduleCalculationRule = reviewScheduleDateCalculationService.determineReviewScheduleCalculationRule(
        prisonNumber = prisonNumber,
        sentenceType = sentenceType,
        releaseDate = releaseDate,
      )
      val reviewScheduleWindow = reviewScheduleDateCalculationService.calculateReviewWindow(reviewScheduleCalculationRule, releaseDate)

      CompletedReviewDto(
        completedReview = completedReview,
        wasLastReviewBeforeRelease = reviewScheduleWindow == null,
        latestReviewSchedule = reviewScheduleWindow?.let {
          // If a new ReviewScheduleWindow was calculated, create a new ReviewSchedule with it
          reviewSchedulePersistenceAdapter.createReviewSchedule(
            CreateReviewScheduleDto(
              prisonNumber = prisonNumber,
              prisonId = prisonId,
              reviewScheduleWindow = it,
              scheduleCalculationRule = reviewScheduleCalculationRule,
            ),
          )
        } ?: let {
          // No new ReviewScheduleWindow was calculated, so this was the prisoners last Review before release
          // update the completed review to have preRelease flag set to true:
          reviewPersistenceAdapter.setPreRelease(completedReview.reference)
          completedReviewSchedule!!
        },
      ).also {
        reviewEventService.reviewCompleted(it.completedReview)
      }
    }
}
