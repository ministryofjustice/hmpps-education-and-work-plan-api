package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule.MORE_THAN_60_MONTHS_TO_SERVE
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule.PRISONER_ON_REMAND
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule.PRISONER_READMISSION
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule.PRISONER_TRANSFER
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule.PRISONER_UN_SENTENCED
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleWindow
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType.CONVICTED_UNSENTENCED
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType.INDETERMINATE_SENTENCE
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType.REMAND
import java.time.LocalDate

private val log = KotlinLogging.logger {}

/**
 * Service class exposing methods that implement the business rules for calculating Review Schedule dates.
 *
 * This class is deliberately final so that it cannot be subclassed, ensuring that the business rules stay within the
 * domain.
 */
class ReviewScheduleDateCalculationService {
  companion object {
    private const val EXEMPTION_ADDITIONAL_DAYS = 5L
    private const val EXCLUSION_ADDITIONAL_DAYS = 10L
    private const val SYSTEM_OUTAGE_ADDITIONAL_DAYS = 5L
    private const val RESCHEDULE_ADDITIONAL_DAYS = 10L
    private const val TEN_DAYS = 10L
  }

  /**
   * Returns the [ReviewScheduleCalculationRule] based on the combination of the specified parameters, which can in turn
   * be used to calculate the Review Schedule Window (see [ReviewScheduleDateCalculationService.calculateReviewWindow])
   */
  fun determineReviewScheduleCalculationRule(
    prisonNumber: String,
    sentenceType: SentenceType,
    releaseDate: LocalDate?,
    isReAdmission: Boolean = false,
    isTransfer: Boolean = false,
  ): ReviewScheduleCalculationRule =
    if (isReAdmission) {
      PRISONER_READMISSION
    } else if (isTransfer) {
      PRISONER_TRANSFER
    } else {
      when (sentenceType) {
        REMAND -> PRISONER_ON_REMAND
        CONVICTED_UNSENTENCED -> PRISONER_UN_SENTENCED
        INDETERMINATE_SENTENCE -> ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE
        else -> if (releaseDate != null) {
          reviewScheduleCalculationRuleBasedOnTimeLeftToServe(releaseDate)
        } else {
          ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE
        }
      }
    }

  /**
   * Returns a [ReviewScheduleWindow] based on the specified [ReviewScheduleCalculationRule] and prisoner release date.
   * If the prisoner has less than 3 months to serve ([ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE])
   * they are not due a Review and `null` is returned.
   * In all other cases an appropriate [ReviewScheduleWindow] is returned.
   */
  fun calculateReviewWindow(
    reviewScheduleCalculationRule: ReviewScheduleCalculationRule,
    releaseDate: LocalDate?,
  ): ReviewScheduleWindow? =
    when (reviewScheduleCalculationRule) {
      BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE -> null
      PRISONER_READMISSION, PRISONER_TRANSFER -> ReviewScheduleWindow.fromTodayToTenDays(baseScheduleDate())
      BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE -> {
        // If the prisoner has between 3 months and 3 months 7 days left to serve their Review Schedule Window would be between 1 and 3 months
        // as they would fall into the "between 3 and 6 months left to serve" rule. This would mean their deadline date would be within the last
        // week before release.
        // Prisoners and CIAGs need a clear 7 days between their final review deadline and release (their last week in prison is busy), so
        // we need to set the deadline date to be the prisoner's release date minus 7 days
        ReviewScheduleWindow.fromOneMonthToSpecificDate(baseScheduleDate(), releaseDate!!.minusDays(7))
      }

      BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE -> ReviewScheduleWindow.fromOneToThreeMonths(baseScheduleDate())
      BETWEEN_6_AND_12_MONTHS_TO_SERVE, PRISONER_ON_REMAND, PRISONER_UN_SENTENCED -> ReviewScheduleWindow.fromTwoToThreeMonths(baseScheduleDate())
      BETWEEN_12_AND_60_MONTHS_TO_SERVE -> ReviewScheduleWindow.fromFourToSixMonths(baseScheduleDate())
      MORE_THAN_60_MONTHS_TO_SERVE, ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE -> ReviewScheduleWindow.fromTenToTwelveMonths(baseScheduleDate())
    }.also {
      when {
        it == null -> log.debug { "Returning no ReviewScheduleWindow because ReviewScheduleCalculationRule is $reviewScheduleCalculationRule" }
        else -> log.debug { "Returning ReviewScheduleWindow $it based on ReviewScheduleCalculationRule $reviewScheduleCalculationRule" }
      }
    }

  /**
   * When clearing an exemption on a [ReviewSchedule] the Review Due date might need to be adjusted. This is to allow for
   * clearing the exemption of a [ReviewSchedule] after it's original Review Due date. IE. it would immediately become
   * overdue.
   * Exemptions are classed as either "exemptions" or "exclusions" (via the [ReviewScheduleStatus] enum), and the amount
   * of time to adjust a Review Due date by is different for "exemptions" and "exclusions".
   * If the original due date is later than the calculated date, then the original due date should be used, and the
   * Review Schedule should not get an adjusted due date. This is to help prevent a Review being extended repeatedly to
   * prevent it becoming overdue by repeatedly exempting and clearing the exemption.
   *
   * Returns a [LocalDate] that can be used as an adjusted Review Due date, based on whether specified [ReviewSchedule]
   * has a status that is an "exclusion" or an "exemption", and whether the calculated date is later than the existing
   * due date.
   */
  fun calculateAdjustedReviewDueDate(reviewSchedule: ReviewSchedule): LocalDate =
    with(reviewSchedule) {
      if (reviewSchedule.scheduleStatus == ReviewScheduleStatus.EXEMPT_PRISONER_TRANSFER) {
        return LocalDate.now().plusDays(TEN_DAYS)
      }
      val additionalDays = getExtensionDays(scheduleStatus)
      val todayPlusAdditionalDays = LocalDate.now().plusDays(additionalDays)
      maxOf(todayPlusAdditionalDays, reviewScheduleWindow.dateTo)
    }

  private fun getExtensionDays(status: ReviewScheduleStatus): Long =
    when {
      status == ReviewScheduleStatus.EXEMPT_UNKNOWN -> RESCHEDULE_ADDITIONAL_DAYS
      status == ReviewScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE -> SYSTEM_OUTAGE_ADDITIONAL_DAYS
      status.isExclusion -> EXCLUSION_ADDITIONAL_DAYS
      status.isExemption -> EXEMPTION_ADDITIONAL_DAYS
      else -> 0 // Default case, if no condition matches
    }

  private fun reviewScheduleCalculationRuleBasedOnTimeLeftToServe(releaseDate: LocalDate): ReviewScheduleCalculationRule {
    val timeLeftToServe = from(baseScheduleDate()).until(releaseDate)

    return when {
      timeLeftToServe.isNoMoreThan3Months() -> BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE
      timeLeftToServe.isBetween3MonthsAnd3Months7Days() -> BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE
      timeLeftToServe.isBetween3Months8DaysAnd6Months() -> BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE
      timeLeftToServe.isBetween6And12Months() -> BETWEEN_6_AND_12_MONTHS_TO_SERVE
      timeLeftToServe.isBetween12And60Months() -> BETWEEN_12_AND_60_MONTHS_TO_SERVE
      timeLeftToServe.isMoreThan60Months() -> MORE_THAN_60_MONTHS_TO_SERVE
      else -> MORE_THAN_60_MONTHS_TO_SERVE
    }.also {
      log.debug { "Returning ReviewScheduleCalculationRule $it based on release date of $releaseDate" }
    }
  }

  /**
   * Returns the base date from which all Induction Schedule dates are calculated
   */
  private fun baseScheduleDate() = LocalDate.now()
}
