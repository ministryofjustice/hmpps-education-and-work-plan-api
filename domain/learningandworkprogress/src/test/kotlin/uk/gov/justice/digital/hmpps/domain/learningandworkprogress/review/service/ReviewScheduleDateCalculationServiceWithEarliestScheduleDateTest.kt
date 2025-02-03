package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleWindow
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import java.time.LocalDate
import java.util.stream.Stream

/**
 * Unit test class for [ReviewScheduleDateCalculationService] that uses a date as the earliest date from which
 * all calculated schedule dates are set.
 *
 * The concept of the earliest schedule is specifically to support creating Review Schedules before the official go live
 * on 2025-04-1
 * After 2025-04-01, once the feature has gone live, the optional constructor arg and associated code will be removed,
 * at which point this unit test can be removed.
 */
class ReviewScheduleDateCalculationServiceWithEarliestScheduleDateTest {
  private val dateCalculationService = ReviewScheduleDateCalculationService(APRIL_FIRST)

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("determineReviewScheduleCalculationRule_testCases")
  fun `should determine review schedule calculation rule`(
    scenario: String,
    releaseDate: LocalDate?,
    sentenceType: SentenceType,
    isReadmission: Boolean,
    isTransfer: Boolean,
    expectedReviewScheduleCalculationRule: ReviewScheduleCalculationRule,
  ) {
    // Given

    // When
    val actual = dateCalculationService.determineReviewScheduleCalculationRule(
      PRISON_NUMBER,
      sentenceType,
      releaseDate,
      isReadmission,
      isTransfer,
    )

    // Then
    assertThat(actual).isEqualTo(expectedReviewScheduleCalculationRule)
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("calculateReviewScheduleWindow_testCases")
  fun `should calculate a review schedule window`(
    scenario: String,
    releaseDate: LocalDate,
    expectedReviewScheduleWindow: ReviewScheduleWindow?,
  ) {
    // Given

    // When
    val reviewScheduleCalculationRule = dateCalculationService.determineReviewScheduleCalculationRule(
      PRISON_NUMBER,
      SentenceType.SENTENCED,
      releaseDate,
      false,
      false,
    )
    val actual = dateCalculationService.calculateReviewWindow(reviewScheduleCalculationRule, releaseDate)

    // Then
    assertThat(actual).isEqualTo(expectedReviewScheduleWindow)
  }

  // Calculate a review schedule window as if the calculation was being performed on the earliest calculation date (April 1st)
  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("calculateReviewScheduleWindow_specificTestCasesFromBA")
  fun `should calculate a review schedule window using specific test data scenarios from the BA`(
    scenario: String,
    releaseDate: LocalDate,
    expectedReviewScheduleWindow: ReviewScheduleWindow?,
  ) {
    // Given

    // When
    val reviewScheduleCalculationRule = dateCalculationService.determineReviewScheduleCalculationRule(
      PRISON_NUMBER,
      SentenceType.SENTENCED,
      releaseDate,
      false,
      false,
    )
    val actual = dateCalculationService.calculateReviewWindow(reviewScheduleCalculationRule, releaseDate)

    // Then
    assertThat(actual).isEqualTo(expectedReviewScheduleWindow)
  }

  companion object {
    private val APRIL_FIRST = LocalDate.parse("2025-04-01")
    private val PRISON_NUMBER = randomValidPrisonNumber()

    @JvmStatic
    fun calculateReviewScheduleWindow_specificTestCasesFromBA(): Stream<Arguments> =
      Stream.of(
        Arguments.of(
          "prisoner release date 27th July, just under 4 months to serve from April 1st - review schedule window 1-3 months from April 1st (May 1st to July 1st)",
          LocalDate.parse("2025-07-27"),
          ReviewScheduleWindow(LocalDate.parse("2025-05-01"), LocalDate.parse("2025-07-01")),
        ),
        Arguments.of(
          "prisoner release date 27th October, just under 7 months to serve from April 1st - review schedule window 2-3 months from April 1st (June 1st to July 1st)",
          LocalDate.parse("2025-10-27"),
          ReviewScheduleWindow(LocalDate.parse("2025-06-01"), LocalDate.parse("2025-07-01")),
        ),
        Arguments.of(
          "prisoner release date 3rd April, less than 3 months to serve from April 1st - no review schedule window required",
          LocalDate.parse("2025-04-03"),
          null,
        ),
        Arguments.of(
          "prisoner release date 1st July, exactly 3 months to serve from April 1st - no review schedule window required",
          LocalDate.parse("2025-07-01"),
          null,
        ),
        Arguments.of(
          "prisoner release date 2nd July, exactly 3 months and 1 day to serve from April 1st - review schedule window 1-2 months minus 7 days from April 1st (May 1st to June 25th)",
          LocalDate.parse("2025-07-02"),
          ReviewScheduleWindow(LocalDate.parse("2025-05-01"), LocalDate.parse("2025-06-25")),
        ),
        Arguments.of(
          "prisoner release date 8th July, exactly 3 months and 8 days to serve from April 1st - review schedule window 1-3 months from April 1st (May 1st to July 1st)",
          LocalDate.parse("2025-07-08"),
          ReviewScheduleWindow(LocalDate.parse("2025-05-01"), LocalDate.parse("2025-07-01")),
        ),
        Arguments.of(
          "prisoner release date 1st February 2026, 10 months to serve from April 1st - review schedule window 2-3 months from April 1st (June 1st to July 1st)",
          LocalDate.parse("2026-02-01"),
          ReviewScheduleWindow(LocalDate.parse("2025-06-01"), LocalDate.parse("2025-07-01")),
        ),
        Arguments.of(
          "prisoner release date 1st May 2026, 13 months to serve from April 1st - review schedule window 4-6 months from April 1st (August 1st to October 1st)",
          LocalDate.parse("2026-05-01"),
          ReviewScheduleWindow(LocalDate.parse("2025-08-01"), LocalDate.parse("2025-10-01")),
        ),
        Arguments.of(
          "prisoner release date 1st April 2027, 24 months to serve from April 1st - review schedule window 4-6 months from April 1st (August 1st to October 1st)",
          LocalDate.parse("2027-04-01"),
          ReviewScheduleWindow(LocalDate.parse("2025-08-01"), LocalDate.parse("2025-10-01")),
        ),
        Arguments.of(
          "prisoner release date 1st April 2030, exactly 60 months to serve from April 1st - review schedule window 4-6 months from April 1st (August 1st to October 1st)",
          LocalDate.parse("2030-04-01"),
          ReviewScheduleWindow(LocalDate.parse("2025-08-01"), LocalDate.parse("2025-10-01")),
        ),
        Arguments.of(
          "prisoner release date 2nd April 2030, over 60 months to serve from April 1st - review schedule window 10-12 months from April 1st (February 1st to April 1st)",
          LocalDate.parse("2030-04-02"),
          ReviewScheduleWindow(LocalDate.parse("2026-02-01"), LocalDate.parse("2026-04-01")),
        ),
      )

    @JvmStatic
    fun calculateReviewScheduleWindow_testCases(): Stream<Arguments> =
      Stream.of(
        Arguments.of(
          "prisoner has less than 3 months left to serve on April 1st - no review schedule window",
          APRIL_FIRST.plusMonths(3).minusDays(1),
          null,
        ),
        Arguments.of(
          "prisoner has exactly 3 months left to serve on April 1st - no review schedule window",
          APRIL_FIRST.plusMonths(3),
          null,
        ),
        Arguments.of(
          "prisoner has between 3 months and 3 months and 7 days left to serve on April 1st - review schedule window from 1 to 3 months from April 1st",
          APRIL_FIRST.plusMonths(3).plusDays(7),
          ReviewScheduleWindow(APRIL_FIRST.plusMonths(1), APRIL_FIRST.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner has between 3 months 8 days and 6 months left to serve on April 1st - review schedule window from 1 to 3 months from April 1st",
          APRIL_FIRST.plusMonths(3).plusDays(7),
          ReviewScheduleWindow(APRIL_FIRST.plusMonths(1), APRIL_FIRST.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner has between 6 and 12 months left to serve on April 1st - review schedule window from 2 to 3 months from April 1st",
          APRIL_FIRST.plusMonths(7),
          ReviewScheduleWindow(APRIL_FIRST.plusMonths(2), APRIL_FIRST.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner has between 12 and 60 months left to serve on April 1st - review schedule window from 4 to 6 months from April 1st",
          APRIL_FIRST.plusMonths(48),
          ReviewScheduleWindow(APRIL_FIRST.plusMonths(4), APRIL_FIRST.plusMonths(6)),
        ),
        Arguments.of(
          "prisoner has more than 60 months left to serve on April 1st - review schedule window from 10 to 12 months from April 1st",
          APRIL_FIRST.plusMonths(62),
          ReviewScheduleWindow(APRIL_FIRST.plusMonths(10), APRIL_FIRST.plusMonths(12)),
        ),
      )

    @JvmStatic
    fun determineReviewScheduleCalculationRule_testCases(): Stream<Arguments> =
      Stream.of(
        Arguments.of(
          "prisoner is a transfer",
          APRIL_FIRST.plusMonths(24),
          SentenceType.SENTENCED,
          false,
          true,
          ReviewScheduleCalculationRule.PRISONER_TRANSFER,
        ),
        Arguments.of(
          "prisoner is a readmission",
          APRIL_FIRST.plusMonths(24),
          SentenceType.SENTENCED,
          true,
          false,
          ReviewScheduleCalculationRule.PRISONER_READMISSION,
        ),
        Arguments.of(
          "prisoner is on remand",
          null,
          SentenceType.REMAND,
          false,
          false,
          ReviewScheduleCalculationRule.PRISONER_ON_REMAND,
        ),
        Arguments.of(
          "prisoner is convicted un-sentenced",
          null,
          SentenceType.CONVICTED_UNSENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.PRISONER_UN_SENTENCED,
        ),
        Arguments.of(
          "prisoner has indeterminate sentence",
          null,
          SentenceType.INDETERMINATE_SENTENCE,
          false,
          false,
          ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE,
        ),
        Arguments.of(
          "prisoner is sentenced with 1 day less than 3 months left to serve on April 1st",
          APRIL_FIRST.plusMonths(3).minusDays(1),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with exactly 3 months left to serve on April 1st",
          APRIL_FIRST.plusMonths(3),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with 3 months and 1 day left to serve on April 1st",
          APRIL_FIRST.plusMonths(3).plusDays(1),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with 3 months and 7 days left to serve on April 1st",
          APRIL_FIRST.plusMonths(3).plusDays(7),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with 3 months and 8 days left to serve on April 1st",
          APRIL_FIRST.plusMonths(3).plusDays(8),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with exactly 6 months left to serve on April 1st",
          APRIL_FIRST.plusMonths(6),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with 6 months and 1 day left to serve on April 1st",
          APRIL_FIRST.plusMonths(6).plusDays(1),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with exactly 12 months left to serve on April 1st",
          APRIL_FIRST.plusMonths(12),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with 12 months and 1 day left to serve on April 1st",
          APRIL_FIRST.plusMonths(12).plusDays(1),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with exactly 60 months left to serve on April 1st",
          APRIL_FIRST.plusMonths(60),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with more than 60 months left to serve on April 1st",
          APRIL_FIRST.plusMonths(60).plusDays(1),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.MORE_THAN_60_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with a release date of before April 1st (ie. will be released before April 1st)",
          APRIL_FIRST.minusDays(1),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE,
        ),
      )
  }
}
