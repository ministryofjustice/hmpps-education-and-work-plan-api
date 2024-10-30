package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.CompletedReview

interface ReviewPersistenceAdapter {
  /**
   * Returns a [List] of [CompletedReview]s for the prisoner. An empty list is returned if there are no Completed Reviews
   * for the prisoner.
   */
  fun getCompletedReviews(prisonNumber: String): List<CompletedReview>
}
