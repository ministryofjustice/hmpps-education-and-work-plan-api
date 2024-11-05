package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.CompletedReview
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
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
    val timeLeftToServeInMonths = timeLeftToServe.toTotalMonths()
    val timeLeftToServeRemainderDays = timeLeftToServe.minusMonths(timeLeftToServeInMonths).days

    return if (timeLeftToServeInMonths < 6 || (timeLeftToServeInMonths == 6L && timeLeftToServeRemainderDays == 0)) {
      ReviewScheduleCalculationRule.LESS_THAN_6_MONTHS_TO_SERVE
    } else if (timeLeftToServeInMonths < 12 || (timeLeftToServeInMonths == 12L && timeLeftToServeRemainderDays == 0)) {
      ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE
    } else if (timeLeftToServeInMonths < 60 || (timeLeftToServeInMonths == 60L && timeLeftToServeRemainderDays == 0)) {
      ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE
    } else {
      ReviewScheduleCalculationRule.MORE_THAN_60_MONTHS_TO_SERVE
    }
  }
}

enum class SentenceType {
  RECALL,
  DEAD,
  INDETERMINATE_SENTENCE,
  SENTENCED,
  CONVICTED_UNSENTENCED,
  CIVIL_PRISONER,
  IMMIGRATION_DETAINEE,
  REMAND,
  UNKNOWN,
  OTHER,
}

data class ReviewScheduleWindow(
  val dateFrom: LocalDate,
  val dateTo: LocalDate,
) {
  companion object {
    fun fromTodayToTenDays(): ReviewScheduleWindow = with(LocalDate.now()) { ReviewScheduleWindow(this, this.plusDays(10)) }
    fun fromOneToThreeMonths(): ReviewScheduleWindow = with(LocalDate.now()) { ReviewScheduleWindow(this.plusMonths(1), this.plusMonths(3)) }
    fun fromTwoToThreeMonths(): ReviewScheduleWindow = with(LocalDate.now()) { ReviewScheduleWindow(this.plusMonths(2), this.plusMonths(3)) }
    fun fromFourToSixMonths(): ReviewScheduleWindow = with(LocalDate.now()) { ReviewScheduleWindow(this.plusMonths(4), this.plusMonths(6)) }
    fun fromTenToTwelveMonths(): ReviewScheduleWindow = with(LocalDate.now()) { ReviewScheduleWindow(this.plusMonths(10), this.plusMonths(12)) }
  }
}
