package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleWindow
import java.time.LocalDate

fun aValidCreateReviewScheduleDto(
  prisonNumber: String = "A1234BC",
  reviewScheduleWindow: ReviewScheduleWindow = ReviewScheduleWindow.fromFourToSixMonths(LocalDate.now()),
  scheduleCalculationRule: ReviewScheduleCalculationRule = ReviewScheduleCalculationRule.MORE_THAN_60_MONTHS_TO_SERVE,
  prisonId: String = "BXI",
): CreateReviewScheduleDto =
  CreateReviewScheduleDto(
    prisonNumber = prisonNumber,
    reviewScheduleWindow = reviewScheduleWindow,
    scheduleCalculationRule = scheduleCalculationRule,
    prisonId = prisonId,
  )
