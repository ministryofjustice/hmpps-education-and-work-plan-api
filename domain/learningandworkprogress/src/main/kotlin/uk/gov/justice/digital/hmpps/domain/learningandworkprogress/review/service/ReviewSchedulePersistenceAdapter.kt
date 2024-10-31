package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule

interface ReviewSchedulePersistenceAdapter {
  /**
   * Retrieves a [ReviewSchedule] for a given Prisoner. Returns `null` if the [ReviewSchedule] does not exist.
   */
  fun getReviewSchedule(prisonNumber: String): ReviewSchedule?
}
