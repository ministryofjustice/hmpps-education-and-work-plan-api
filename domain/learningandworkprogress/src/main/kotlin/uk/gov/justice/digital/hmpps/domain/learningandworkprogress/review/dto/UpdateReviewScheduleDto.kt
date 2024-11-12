package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto

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
)
