package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.CompletedReview

/**
 * Interface defining a series of Review lifecycle event methods.
 */
interface ReviewEventService {

  /**
   * Implementations providing custom code for when a Review is completed.
   */
  fun reviewCompleted(completedReview: CompletedReview)
}
