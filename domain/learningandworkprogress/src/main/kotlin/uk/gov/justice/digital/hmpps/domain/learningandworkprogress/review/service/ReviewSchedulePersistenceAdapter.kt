package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ActiveReviewScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleHistory
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.UpdateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.UpdateReviewScheduleStatusDto

interface ReviewSchedulePersistenceAdapter {

  /**
   * Persists a new [ReviewSchedule].
   *
   * Throws [ActiveReviewScheduleAlreadyExistsException] if the prisoner already has an active [ReviewSchedule].
   */
  fun createReviewSchedule(createReviewScheduleDto: CreateReviewScheduleDto): ReviewSchedule

  /**
   * Updates a [ReviewSchedule] identified by its `reference`.
   */
  fun updateReviewSchedule(updateReviewScheduleDto: UpdateReviewScheduleDto): ReviewSchedule?

  /**
   * Returns the prisoner's active Review Schedule, where "active" is defined as not having the status
   * "COMPLETED", "EXEMPT_PRISONER_RELEASE" or "EXEMPT_PRISONER_DEATH".
   *
   * A prisoner will have many Review Schedule records, but only one of them will be their "active" one. It is not
   * possible for a prisoner to have more than one active Review Schedule.
   *
   * A Review Schedule with the status SCHEDULED or one of the EXEMPT_ statuses is considered the active Review Schedule.
   * A Review Schedule with one of the EXEMPT_ statuses is still consider active as the exemption can be cleared at which
   * point its status becomes SCHEDULED again. The exceptions to this are EXEMPT_PRISONER_RELEASE, EXEMPT_PRISONER_DEATH
   * and EXEMPT_UNKNOWN, none of which can be cleared.
   *
   * Returns `null` if the active [ReviewSchedule] does not exist.
   */
  fun getActiveReviewSchedule(prisonNumber: String): ReviewSchedule?

  /**
   * Retrieves a Prisoner's latest [ReviewSchedule]. The latest (most recently updated) [ReviewSchedule] is returned
   * irrespective of status.
   * Returns `null` if no [ReviewSchedule]s exist.
   */
  fun getLatestReviewSchedule(prisonNumber: String): ReviewSchedule?

  /**
   * Retrieves the history of a Prisoner's [ReviewSchedule]s.
   */
  fun getReviewScheduleHistory(prisonNumber: String): List<ReviewScheduleHistory>

  /**
   * Update the Review schedule status, prisonId and if present the latestReviewDate.
   */
  fun updateReviewScheduleStatus(updateReviewScheduleStatusDto: UpdateReviewScheduleStatusDto): ReviewSchedule
}
