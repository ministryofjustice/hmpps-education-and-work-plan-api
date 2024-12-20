package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleWindow

fun aValidCreateReviewScheduleDto(
  prisonNumber: String = "A1234BC",
  reviewScheduleWindow: ReviewScheduleWindow = ReviewScheduleWindow.fromFourToSixMonths(),
  scheduleCalculationRule: ReviewScheduleCalculationRule = ReviewScheduleCalculationRule.MORE_THAN_60_MONTHS_TO_SERVE,
  prisonId: String = "BXI",
): CreateReviewScheduleDto =
  CreateReviewScheduleDto(
    prisonNumber = prisonNumber,
    reviewScheduleWindow = reviewScheduleWindow,
    scheduleCalculationRule = scheduleCalculationRule,
    prisonId = prisonId,
  )
