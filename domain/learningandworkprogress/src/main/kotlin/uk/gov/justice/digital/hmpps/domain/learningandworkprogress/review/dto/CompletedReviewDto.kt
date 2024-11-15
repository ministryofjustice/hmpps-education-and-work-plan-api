package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.CompletedReview
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule

data class CompletedReviewDto(
  val completedReview: CompletedReview,
  val wasLastReviewBeforeRelease: Boolean,
  val latestReviewSchedule: ReviewSchedule,
)
