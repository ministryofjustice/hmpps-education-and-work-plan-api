package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.CompletedReview
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleWindow
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CompletedReviewDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateCompletedReviewDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateInitialReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.UpdateReviewScheduleDto
import java.time.LocalDate
import java.time.Period

private val log = KotlinLogging.logger {}

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
  private val reviewEventService: ReviewEventService,
  private val reviewPersistenceAdapter: ReviewPersistenceAdapter,
  private val reviewSchedulePersistenceAdapter: ReviewSchedulePersistenceAdapter,
) {

  /**
   * Returns the active [ReviewSchedule] for the prisoner identified by their prison number, where "active" is defined
   * as not having the status "COMPLETED".
   * Otherwise, throws [ReviewScheduleNotFoundException] if it cannot be found.
   */
  fun getActiveReviewScheduleForPrisoner(prisonNumber: String): ReviewSchedule =
    reviewSchedulePersistenceAdapter.getActiveReviewSchedule(prisonNumber) ?: throw ReviewScheduleNotFoundException(
      prisonNumber,
    )

  /**
   * Returns the latest [ReviewSchedule] for the prisoner identified by their prison number. The latest (most recently
   * updated) [ReviewSchedule] is returned irrespective of status.
   * Otherwise, throws [ReviewScheduleNotFoundException] if it cannot be found.
   */
  fun getLatestReviewScheduleForPrisoner(prisonNumber: String): ReviewSchedule =
    reviewSchedulePersistenceAdapter.getLatestReviewSchedule(prisonNumber) ?: throw ReviewScheduleNotFoundException(
      prisonNumber,
    )

  /**
   * Returns a list of all [CompletedReview]s for the prisoner identified by their prison number. An empty list is
   * returned if the prisoner has no Completed Reviews.
   */
  fun getCompletedReviewsForPrisoner(prisonNumber: String): List<CompletedReview> =
    reviewPersistenceAdapter.getCompletedReviews(prisonNumber)

  /**
   * Creates a [CompletedReview] for the prisoner. and updates their current [ReviewSchedule] to be COMPLETED.
   *
   * Returns a [CompletedReviewDto] containing the completed review, a flag indicating whether this was the prisoner's
   * last review or not, and the prisoners latest [ReviewSchedule].
   *
   * If this was the prisoner's last review before release a new [ReviewSchedule] is not created, and their latest
   * [ReviewSchedule] is the one that was updated to be COMPLETED.
   * If this is not the prisoner's last review and a new Review Schedule Window is calculated for them, a new [ReviewSchedule]
   * is created with a status of SCHEDULED. This therefore becomes their latest Review Schedule.
   *
   * If the prisoner does not already have an active [ReviewSchedule] a [ReviewScheduleNotFoundException] is thrown.
   */
  fun createReview(createCompletedReviewDto: CreateCompletedReviewDto): CompletedReviewDto {
    // Get the active review schedule
    val currentReviewSchedule = getActiveReviewScheduleForPrisoner(createCompletedReviewDto.prisonNumber)

    // Persist a new CompletedReview
    val completedReview = reviewPersistenceAdapter.createCompletedReview(
      createCompletedReviewDto = createCompletedReviewDto,
      reviewSchedule = currentReviewSchedule,
    )

    // Update the current review schedule to mark it as Completed
    val completedReviewSchedule = reviewSchedulePersistenceAdapter.updateReviewSchedule(
      UpdateReviewScheduleDto.setStatusToCompletedAtPrison(currentReviewSchedule, createCompletedReviewDto.prisonId),
    )

    // Work out the next review schedule calculation rule and schedule window
    val releaseDate = createCompletedReviewDto.prisonerReleaseDate
    val reviewScheduleCalculationRule = determineReviewScheduleCalculationRuleBasedOnSentenceTypeAndReleaseDate(
      sentenceType = createCompletedReviewDto.prisonerSentenceType,
      releaseDate = releaseDate,
    )
    val reviewScheduleWindow = calculateReviewWindow(reviewScheduleCalculationRule, releaseDate)

    return CompletedReviewDto(
      completedReview = completedReview,
      wasLastReviewBeforeRelease = reviewScheduleWindow == null,
      latestReviewSchedule = reviewScheduleWindow?.let {
        // If a new ReviewScheduleWindow was calculated, create a new ReviewSchedule with it
        reviewSchedulePersistenceAdapter.createReviewSchedule(
          CreateReviewScheduleDto(
            prisonNumber = createCompletedReviewDto.prisonNumber,
            prisonId = createCompletedReviewDto.prisonId,
            reviewScheduleWindow = it,
            scheduleCalculationRule = reviewScheduleCalculationRule,
            scheduleStatus = SCHEDULED,
          ),
        )
      } ?: let {
        // No new ReviewScheduleWindow was calculated, so this was the prisoners last Review before release
        completedReviewSchedule!!
      },
    ).also {
      reviewEventService.reviewCompleted(it.completedReview)
    }
  }

  fun createInitialReviewSchedule(createInitialReviewScheduleDto: CreateInitialReviewScheduleDto): ReviewSchedule? {
    // Check for an existing active review schedule
    val existingReviewSchedule = runCatching {
      getActiveReviewScheduleForPrisoner(createInitialReviewScheduleDto.prisonNumber)
    }.getOrNull()

    if (existingReviewSchedule != null) {
      throw RuntimeException("An active review schedule already exists for prisoner ${createInitialReviewScheduleDto.prisonNumber}.")
    }

    // Calculate the review schedule window and rule
    val releaseDate = createInitialReviewScheduleDto.prisonerReleaseDate
    val sentenceType = createInitialReviewScheduleDto.prisonerSentenceType
    val reviewScheduleCalculationRule = determineReviewScheduleCalculationRule(
      sentenceType = sentenceType,
      releaseDate = releaseDate,
      isReAdmission = createInitialReviewScheduleDto.isReadmission,
      isTransfer = createInitialReviewScheduleDto.isTransfer,
    )
    // Persist the initial review schedule if a ReviewScheduleWindow is calculated
    return calculateReviewWindow(reviewScheduleCalculationRule, releaseDate)
      ?.let {
        reviewSchedulePersistenceAdapter.createReviewSchedule(
          CreateReviewScheduleDto(
            prisonNumber = createInitialReviewScheduleDto.prisonNumber,
            prisonId = createInitialReviewScheduleDto.prisonId,
            reviewScheduleWindow = it,
            scheduleCalculationRule = reviewScheduleCalculationRule,
            scheduleStatus = SCHEDULED,
          ),
        )
      }
  }

  private fun determineReviewScheduleCalculationRuleBasedOnSentenceTypeAndReleaseDate(
    sentenceType: SentenceType,
    releaseDate: LocalDate?,
  ): ReviewScheduleCalculationRule =
    when (sentenceType) {
      SentenceType.REMAND -> ReviewScheduleCalculationRule.PRISONER_ON_REMAND
      SentenceType.CONVICTED_UNSENTENCED -> ReviewScheduleCalculationRule.PRISONER_UN_SENTENCED
      SentenceType.INDETERMINATE_SENTENCE -> ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE
      SentenceType.SENTENCED, SentenceType.RECALL -> reviewScheduleCalculationRuleBasedOnTimeLeftToServe(releaseDate!!)

      SentenceType.DEAD, SentenceType.CIVIL_PRISONER, SentenceType.IMMIGRATION_DETAINEE, SentenceType.UNKNOWN, SentenceType.OTHER ->
        throw UnsupportedOperationException("Calculating a review schedule for prisoner with sentence type $sentenceType is not supported")
    }

  // TODO - refactor (possibly delete or make private) this method private when calculating the next review for a transfer or readmission has a dedicated service method
  fun determineReviewScheduleCalculationRule(
    releaseDate: LocalDate?,
    sentenceType: SentenceType,
    isReAdmission: Boolean,
    isTransfer: Boolean,
  ): ReviewScheduleCalculationRule =
    if (isReAdmission) {
      ReviewScheduleCalculationRule.PRISONER_READMISSION
    } else if (isTransfer) {
      ReviewScheduleCalculationRule.PRISONER_TRANSFER
    } else {
      determineReviewScheduleCalculationRuleBasedOnSentenceTypeAndReleaseDate(sentenceType, releaseDate)
    }

  // TODO - make this method private when calculating the next review for a transfer or readmission has a dedicated service method
  fun calculateReviewWindow(
    reviewScheduleCalculationRule: ReviewScheduleCalculationRule,
    releaseDate: LocalDate?,
  ): ReviewScheduleWindow? =
    when (reviewScheduleCalculationRule) {
      ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE -> null
      ReviewScheduleCalculationRule.PRISONER_READMISSION, ReviewScheduleCalculationRule.PRISONER_TRANSFER -> ReviewScheduleWindow.fromTodayToTenDays()
      ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE -> {
        // If the prisoner has between 3 months and 3 months 7 days left to serve their Review Schedule Window would be between 1 and 3 months
        // as they would fall into the "between 3 and 6 months left to serve" rule. This would mean their deadline date would be within the last
        // week before release.
        // Prisoners and CIAGs need a clear 7 days between their final review deadline and release (their last week in prison is busy), so
        // we need to reduce the deadline date to give 7 days before release.
        val timeLeftToServe = MonthsAndDaysLeftToServe.until(releaseDate!!)
        ReviewScheduleWindow.fromOneToThreeMonthsMinusDays(8 - timeLeftToServe.days)
      }

      ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE -> ReviewScheduleWindow.fromOneToThreeMonths()
      ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE, ReviewScheduleCalculationRule.PRISONER_ON_REMAND, ReviewScheduleCalculationRule.PRISONER_UN_SENTENCED -> ReviewScheduleWindow.fromTwoToThreeMonths()
      ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE -> ReviewScheduleWindow.fromFourToSixMonths()
      ReviewScheduleCalculationRule.MORE_THAN_60_MONTHS_TO_SERVE, ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE -> ReviewScheduleWindow.fromTenToTwelveMonths()
    }.also {
      when {
        it == null -> log.debug { "Returning no ReviewScheduleWindow because ReviewScheduleCalculationRule is $reviewScheduleCalculationRule" }
        else -> log.debug { "Returning ReviewScheduleWindow $it based on ReviewScheduleCalculationRule $reviewScheduleCalculationRule" }
      }
    }

  private fun reviewScheduleCalculationRuleBasedOnTimeLeftToServe(releaseDate: LocalDate): ReviewScheduleCalculationRule {
    val timeLeftToServe = MonthsAndDaysLeftToServe.until(releaseDate)

    return when {
      timeLeftToServe.isNoMoreThan3Months() -> ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE
      timeLeftToServe.isBetween3MonthsAnd3Months7Days() -> ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE
      timeLeftToServe.isBetween3Months8DaysAnd6Months() -> ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE
      timeLeftToServe.isBetween6And12Months() -> ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE
      timeLeftToServe.isBetween12And60Months() -> ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE
      timeLeftToServe.isMoreThan60Months() -> ReviewScheduleCalculationRule.MORE_THAN_60_MONTHS_TO_SERVE
      else -> ReviewScheduleCalculationRule.MORE_THAN_60_MONTHS_TO_SERVE
    }.also {
      log.debug { "Returning ReviewScheduleCalculationRule $it based on release date of $releaseDate" }
    }
  }

  private data class MonthsAndDaysLeftToServe(
    val months: Long,
    val days: Int,
  ) {
    companion object {
      fun until(releaseDate: LocalDate): MonthsAndDaysLeftToServe {
        val today = LocalDate.now()
        val timeLeftToServe = Period.between(today, releaseDate)
        val monthsLeft = timeLeftToServe.toTotalMonths()
        val remainderDays = timeLeftToServe.minusMonths(monthsLeft).days
        return MonthsAndDaysLeftToServe(monthsLeft, remainderDays)
      }
    }

    private fun isExactMonths(value: Long) = months == value && days == 0

    fun isNoMoreThan3Months(): Boolean = months < 3 || isExactMonths(3)
    fun isBetween3MonthsAnd3Months7Days(): Boolean = months == 3L && days in 1..7
    fun isBetween3Months8DaysAnd6Months(): Boolean =
      ((months == 3L && days >= 8) || months >= 4) && (months < 6 || isExactMonths(6))

    fun isBetween6And12Months(): Boolean = months >= 6 && !isExactMonths(6) && (months < 12 || isExactMonths(12))
    fun isBetween12And60Months(): Boolean = months >= 12 && !isExactMonths(12) && (months < 60 || isExactMonths(60))
    fun isMoreThan60Months(): Boolean = months >= 60 && !isExactMonths(60)
  }
}
