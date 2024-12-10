package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.UpdatedReviewScheduleStatus

/**
 * Interface defining a series of Review schedule lifecycle event methods.
 */
interface ReviewScheduleEventService {

  /**
   * Implementations providing custom code for when a Review schedule is updated.
   */
  fun reviewScheduleStatusUpdated(updatedReviewScheduleStatus: UpdatedReviewScheduleStatus)
}
