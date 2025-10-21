package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleWindow
import java.util.UUID

data class UpdateReviewScheduleDto(
  val reference: UUID,
  val reviewScheduleWindow: ReviewScheduleWindow,
  val scheduleCalculationRule: ReviewScheduleCalculationRule,
  val scheduleStatus: ReviewScheduleStatus,
  val prisonId: String,
) {
  companion object {
    fun setStatusToCompletedAtPrison(reviewSchedule: ReviewSchedule, prisonId: String) = with(reviewSchedule) {
      UpdateReviewScheduleDto(
        reference = reference,
        prisonId = prisonId,
        reviewScheduleWindow = reviewScheduleWindow,
        scheduleCalculationRule = scheduleCalculationRule,
        scheduleStatus = ReviewScheduleStatus.COMPLETED,
      )
    }
  }
}
