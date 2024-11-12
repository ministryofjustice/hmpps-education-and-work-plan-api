package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.UpdateReviewScheduleDto

interface ReviewSchedulePersistenceAdapter {

  /**
   * Persists a new [ReviewSchedule].
   *
   * Throws [ReviewScheduleAlreadyExistsException] if the prisoner already has a [ReviewSchedule].
   */
  fun createReviewSchedule(createReviewScheduleDto: CreateReviewScheduleDto): ReviewSchedule

  /**
   * Updates a [ReviewSchedule] identified by its `reference`.
   */
  fun updateReviewSchedule(updateReviewScheduleDto: UpdateReviewScheduleDto): ReviewSchedule?

  /**
   * Retrieves a [ReviewSchedule] for a given Prisoner. Returns `null` if the [ReviewSchedule] does not exist.
   */
  fun getReviewSchedule(prisonNumber: String): ReviewSchedule?
}
