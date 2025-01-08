package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.UpdatedReviewScheduleStatus

/**
 * Interface defining a series of Review schedule lifecycle event methods.
 */
interface ReviewScheduleEventService {

  /**
   * Implementations providing custom code for when a Review Schedule is created.
   */
  fun reviewScheduleCreated(reviewSchedule: ReviewSchedule)

  /**
   * Implementations providing custom code for when a Review Schedule's status is updated.
   */
  fun reviewScheduleStatusUpdated(updatedReviewScheduleStatus: UpdatedReviewScheduleStatus)
}
