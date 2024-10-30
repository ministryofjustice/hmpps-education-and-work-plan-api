package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.CompletedReview
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException

/**
 * Service class exposing methods that implement the business rules for the Review domain.
 *
 * Applications using Reviews and ReviewSchedules must new up an instance of this class providing an implementation of
 * [ReviewPersistenceAdapter] and [ReviewSchedulePersistenceAdapter].
 *
 * This class is deliberately final so that it cannot be subclassed, ensuring that the business rules stay within the
 * domain.
 */
class ReviewService(
  private val reviewPersistenceAdapter: ReviewPersistenceAdapter,
  private val reviewSchedulePersistenceAdapter: ReviewSchedulePersistenceAdapter,
) {

  /**
   * Returns the [ReviewSchedule] for the prisoner identified by their prison number. Otherwise, throws
   * [ReviewScheduleNotFoundException] if it cannot be found.
   */
  fun getReviewScheduleForPrisoner(prisonNumber: String): ReviewSchedule =
    reviewSchedulePersistenceAdapter.getReviewSchedule(prisonNumber) ?: throw ReviewScheduleNotFoundException(prisonNumber)

  /**
   * Returns a list of all [CompletedReview]s for the prisoner identified by their prison number. An empty list is
   * returned if the prisoner has no Completed Reviews.
   */
  fun getCompletedReviewsForPrisoner(prisonNumber: String): List<CompletedReview> =
    reviewPersistenceAdapter.getCompletedReviews(prisonNumber)
}
