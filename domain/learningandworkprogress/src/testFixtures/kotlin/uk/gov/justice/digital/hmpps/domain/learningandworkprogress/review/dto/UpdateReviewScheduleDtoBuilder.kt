package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleWindow
import java.time.LocalDate
import java.util.UUID

fun aValidUpdateReviewScheduleDto(
  reference: UUID = UUID.randomUUID(),
  reviewScheduleWindow: ReviewScheduleWindow = ReviewScheduleWindow.fromTenToTwelveMonths(LocalDate.now()),
  scheduleCalculationRule: ReviewScheduleCalculationRule = ReviewScheduleCalculationRule.MORE_THAN_60_MONTHS_TO_SERVE,
  scheduleStatus: ReviewScheduleStatus = ReviewScheduleStatus.SCHEDULED,
  prisonId: String = "BXI",
): UpdateReviewScheduleDto =
  UpdateReviewScheduleDto(
    reference = reference,
    reviewScheduleWindow = reviewScheduleWindow,
    scheduleCalculationRule = scheduleCalculationRule,
    scheduleStatus = scheduleStatus,
    prisonId = prisonId,
  )
