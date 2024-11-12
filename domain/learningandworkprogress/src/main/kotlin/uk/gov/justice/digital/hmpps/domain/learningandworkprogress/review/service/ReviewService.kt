package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.CompletedReview
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleWindow
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType
import java.time.LocalDate
import java.time.Period

/**
 * Service class exposing methods that implement the business rules for the Review domain.
 *
 * Applications using Reviews and ReviewSchedules must new up an instance of this class providing an implementation of
 * [ReviewPersistenceAdapter] and [ReviewSchedulePersistenceAdapter].
 *
 * This class is deliberately final so that it cannot be subclassed, ensuring that the business rules stay within the
 * domain.
 */
class ReviewService(
  private val reviewPersistenceAdapter: ReviewPersistenceAdapter,
  private val reviewSchedulePersistenceAdapter: ReviewSchedulePersistenceAdapter,
) {

  /**
   * Returns the [ReviewSchedule] for the prisoner identified by their prison number. Otherwise, throws
   * [ReviewScheduleNotFoundException] if it cannot be found.
   */
  fun getReviewScheduleForPrisoner(prisonNumber: String): ReviewSchedule =
    reviewSchedulePersistenceAdapter.getReviewSchedule(prisonNumber) ?: throw ReviewScheduleNotFoundException(prisonNumber)

  /**
   * Returns a list of all [CompletedReview]s for the prisoner identified by their prison number. An empty list is
   * returned if the prisoner has no Completed Reviews.
   */
  fun getCompletedReviewsForPrisoner(prisonNumber: String): List<CompletedReview> =
    reviewPersistenceAdapter.getCompletedReviews(prisonNumber)

  fun determineReviewScheduleCalculationRule(releaseDate: LocalDate?, sentenceType: SentenceType, isReAdmission: Boolean, isTransfer: Boolean): ReviewScheduleCalculationRule =
    if (isReAdmission) {
      ReviewScheduleCalculationRule.PRISONER_READMISSION
    } else if (isTransfer) {
      ReviewScheduleCalculationRule.PRISONER_TRANSFER
    } else {
      when (sentenceType) {
        SentenceType.REMAND -> ReviewScheduleCalculationRule.PRISONER_ON_REMAND
        SentenceType.CONVICTED_UNSENTENCED -> ReviewScheduleCalculationRule.PRISONER_UN_SENTENCED
        SentenceType.INDETERMINATE_SENTENCE -> ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE

        else -> reviewScheduleCalculationRuleBasedOnTimeLeftToServe(releaseDate!!)
      }
    }

  fun calculateReviewWindow(reviewScheduleCalculationRule: ReviewScheduleCalculationRule): ReviewScheduleWindow =
    when (reviewScheduleCalculationRule) {
      ReviewScheduleCalculationRule.PRISONER_READMISSION, ReviewScheduleCalculationRule.PRISONER_TRANSFER -> ReviewScheduleWindow.fromTodayToTenDays()
      ReviewScheduleCalculationRule.LESS_THAN_6_MONTHS_TO_SERVE -> ReviewScheduleWindow.fromOneToThreeMonths()
      ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE, ReviewScheduleCalculationRule.PRISONER_ON_REMAND, ReviewScheduleCalculationRule.PRISONER_UN_SENTENCED -> ReviewScheduleWindow.fromTwoToThreeMonths()
      ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE -> ReviewScheduleWindow.fromFourToSixMonths()
      ReviewScheduleCalculationRule.MORE_THAN_60_MONTHS_TO_SERVE, ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE -> ReviewScheduleWindow.fromTenToTwelveMonths()
    }

  private fun reviewScheduleCalculationRuleBasedOnTimeLeftToServe(releaseDate: LocalDate): ReviewScheduleCalculationRule {
    val today = LocalDate.now()
    val timeLeftToServe = Period.between(today, releaseDate)
    val monthsLeft = timeLeftToServe.toTotalMonths()
    val remainderDays = timeLeftToServe.minusMonths(monthsLeft).days

    fun isExactMonths(months: Long) = monthsLeft == months && remainderDays == 0

    return when {
      monthsLeft < 6 || isExactMonths(6) -> ReviewScheduleCalculationRule.LESS_THAN_6_MONTHS_TO_SERVE
      monthsLeft < 12 || isExactMonths(12) -> ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE
      monthsLeft < 60 || isExactMonths(60) -> ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE
      else -> ReviewScheduleCalculationRule.MORE_THAN_60_MONTHS_TO_SERVE
    }
  }
}
