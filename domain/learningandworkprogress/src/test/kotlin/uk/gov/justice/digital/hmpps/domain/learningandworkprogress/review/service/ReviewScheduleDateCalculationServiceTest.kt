package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleWindow
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidReviewSchedule
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import java.time.LocalDate
import java.util.stream.Stream

class ReviewScheduleDateCalculationServiceTest {
  private val dateCalculationService = ReviewScheduleDateCalculationService()

  @Nested
  inner class CalculateAdjustedReviewDueDate {
    @ParameterizedTest
    @CsvSource(
      value = [
        "EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY",
        "EXEMPT_PRISONER_OTHER_HEALTH_ISSUES",
        "EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF",
        "EXEMPT_PRISONER_SAFETY_ISSUES",
        "EXEMPT_PRISON_REGIME_CIRCUMSTANCES",
      ],
    )
    fun `should calculate adjusted review due date given an exemption status that is classed as an exclusion and the review due date is later than the calculated date`(
      scheduleStatus: ReviewScheduleStatus,
    ) {
      // Given
      val reviewSchedule = aValidReviewSchedule(
        latestReviewDate = TODAY.plusDays(11),
        scheduleStatus = scheduleStatus,
      )

      val expectedReviewDate = TODAY.plusDays(11)

      // When
      val actual = dateCalculationService.calculateAdjustedReviewDueDate(reviewSchedule)

      // Then
      assertThat(actual).isEqualTo(expectedReviewDate)
    }

    @ParameterizedTest
    @CsvSource(
      value = [
        "EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY",
        "EXEMPT_PRISONER_OTHER_HEALTH_ISSUES",
        "EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF",
        "EXEMPT_PRISONER_SAFETY_ISSUES",
        "EXEMPT_PRISON_REGIME_CIRCUMSTANCES",
      ],
    )
    fun `should calculate adjusted review due date given an exemption status that is classed as an exclusion and the review due date is earlier than the calculated date`(
      scheduleStatus: ReviewScheduleStatus,
    ) {
      // Given
      val reviewSchedule = aValidReviewSchedule(
        latestReviewDate = TODAY.plusDays(9),
        scheduleStatus = scheduleStatus,
      )

      val expectedReviewDate = TODAY.plusDays(10)

      // When
      val actual = dateCalculationService.calculateAdjustedReviewDueDate(reviewSchedule)

      // Then
      assertThat(actual).isEqualTo(expectedReviewDate)
    }

    @ParameterizedTest
    @CsvSource(
      value = [
        "EXEMPT_PRISONER_FAILED_TO_ENGAGE",
        "EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED",
        "EXEMPT_PRISON_STAFF_REDEPLOYMENT",
        "EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE",
        "EXEMPT_PRISONER_RELEASE",
        "EXEMPT_PRISONER_DEATH",
        "EXEMPT_PRISONER_MERGE",
      ],
    )
    fun `should calculate adjusted review due date given an exemption status that is classed as an exemption and the review due date is later than the calculated date`(
      scheduleStatus: ReviewScheduleStatus,
    ) {
      // Given
      val reviewSchedule = aValidReviewSchedule(
        latestReviewDate = TODAY.plusDays(6),
        scheduleStatus = scheduleStatus,
      )

      val expectedReviewDate = TODAY.plusDays(6)

      // When
      val actual = dateCalculationService.calculateAdjustedReviewDueDate(reviewSchedule)

      // Then
      assertThat(actual).isEqualTo(expectedReviewDate)
    }

    @Test
    fun `should calculate adjusted review due date given an exemption status of EXEMPT_PRISONER_TRANSFER`() {
      // Given
      val reviewSchedule = aValidReviewSchedule(
        latestReviewDate = TODAY.plusDays(6),
        scheduleStatus = ReviewScheduleStatus.EXEMPT_PRISONER_TRANSFER,
      )

      val expectedReviewDate = TODAY.plusDays(10)

      // When
      val actual = dateCalculationService.calculateAdjustedReviewDueDate(reviewSchedule)

      // Then
      assertThat(actual).isEqualTo(expectedReviewDate)
    }

    @Test
    fun `should calculate adjusted review due date given an exemption status of EXEMPT_PRISONER_TRANSFER irrespective of original deadline`() {
      // Given
      val reviewSchedule = aValidReviewSchedule(
        latestReviewDate = TODAY.plusDays(29),
        scheduleStatus = ReviewScheduleStatus.EXEMPT_PRISONER_TRANSFER,
      )

      val expectedReviewDate = TODAY.plusDays(10)

      // When
      val actual = dateCalculationService.calculateAdjustedReviewDueDate(reviewSchedule)

      // Then
      assertThat(actual).isEqualTo(expectedReviewDate)
    }

    @ParameterizedTest
    @CsvSource(
      value = [
        "EXEMPT_PRISONER_FAILED_TO_ENGAGE",
        "EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED",
        "EXEMPT_PRISON_STAFF_REDEPLOYMENT",
        "EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE",
        "EXEMPT_PRISONER_RELEASE",
        "EXEMPT_PRISONER_DEATH",
        "EXEMPT_PRISONER_MERGE",
      ],
    )
    fun `should calculate adjusted review due date given an exemption status that is classed as an exemption and the review due date is earlier than the calculated date`(
      scheduleStatus: ReviewScheduleStatus,
    ) {
      // Given
      val reviewSchedule = aValidReviewSchedule(
        latestReviewDate = TODAY.plusDays(4),
        scheduleStatus = scheduleStatus,
      )

      val expectedReviewDate = TODAY.plusDays(5)

      // When
      val actual = dateCalculationService.calculateAdjustedReviewDueDate(reviewSchedule)

      // Then
      assertThat(actual).isEqualTo(expectedReviewDate)
    }

    @ParameterizedTest
    @CsvSource(
      value = [
        "SCHEDULED",
        "COMPLETED",
      ],
    )
    fun `should calculate adjusted review due date given a status that not an exemption status`(
      scheduleStatus: ReviewScheduleStatus,
    ) {
      // Given
      val reviewSchedule = aValidReviewSchedule(
        latestReviewDate = TODAY.plusDays(1),
        scheduleStatus = scheduleStatus,
      )

      val expectedReviewDate = TODAY.plusDays(1)

      // When
      val actual = dateCalculationService.calculateAdjustedReviewDueDate(reviewSchedule)

      // Then
      assertThat(actual).isEqualTo(expectedReviewDate)
    }

    @Test
    fun `should calculate adjusted review due date given EXEMPT_SYSTEM_TECHNICAL_ISSUE and the review due date is later than the calculated date`() {
      // Given
      val reviewSchedule = aValidReviewSchedule(
        latestReviewDate = TODAY.plusDays(6),
        scheduleStatus = ReviewScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE,
      )

      val expectedReviewDate = TODAY.plusDays(6)

      // When
      val actual = dateCalculationService.calculateAdjustedReviewDueDate(reviewSchedule)

      // Then
      assertThat(actual).isEqualTo(expectedReviewDate)
    }

    @Test
    fun `should calculate adjusted review due date given EXEMPT_SYSTEM_TECHNICAL_ISSUE and the review due date is earlier than the calculated date`() {
      // Given
      val reviewSchedule = aValidReviewSchedule(
        latestReviewDate = TODAY.plusDays(4),
        scheduleStatus = ReviewScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE,
      )

      val expectedReviewDate = TODAY.plusDays(5)

      // When
      val actual = dateCalculationService.calculateAdjustedReviewDueDate(reviewSchedule)

      // Then
      assertThat(actual).isEqualTo(expectedReviewDate)
    }
  }

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
    releaseDate: LocalDate?,
    reviewScheduleCalculationRule: ReviewScheduleCalculationRule,
    expectedReviewScheduleWindow: ReviewScheduleWindow?,
  ) {
    // Given

    // When
    val actual = dateCalculationService.calculateReviewWindow(reviewScheduleCalculationRule, releaseDate)

    // Then
    assertThat(actual).isEqualTo(expectedReviewScheduleWindow)
  }

  companion object {
    private val TODAY = LocalDate.now()
    private val PRISON_NUMBER = randomValidPrisonNumber()

    @JvmStatic
    fun calculateReviewScheduleWindow_testCases(): Stream<Arguments> =
      Stream.of(
        Arguments.of(
          "prisoner has less than 3 months left to serve - no review schedule window",
          TODAY.plusMonths(3).minusDays(1),
          ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE,
          null,
        ),
        Arguments.of(
          "prisoner has exactly 3 months left to serve - no review schedule window",
          TODAY.plusMonths(3),
          ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE,
          null,
        ),
        Arguments.of(
          "prisoner transfer - review schedule window from today to today + 10 days",
          TODAY.plusMonths(3),
          ReviewScheduleCalculationRule.PRISONER_TRANSFER,
          ReviewScheduleWindow(TODAY, TODAY.plusDays(10)),
        ),
        Arguments.of(
          "prisoner readmission - review schedule window from today to today + 10 days",
          TODAY.plusMonths(3),
          ReviewScheduleCalculationRule.PRISONER_READMISSION,
          ReviewScheduleWindow(TODAY, TODAY.plusDays(10)),
        ),
        Arguments.of(
          "prisoner has between 3 months and 3 months and 7 days left to serve - review schedule window from 1 to 3 months",
          TODAY.plusMonths(3).plusDays(7),
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(1), TODAY.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner has between 3 months 8 days and 6 months left to serve - review schedule window from 1 to 3 months",
          TODAY.plusMonths(3).plusDays(7),
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(1), TODAY.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner has between 6 and 12 months left to serve - review schedule window from 2 to 3 months",
          TODAY.plusMonths(7),
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(2), TODAY.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner is on remand - review schedule window from 2 to 3 months",
          null,
          ReviewScheduleCalculationRule.PRISONER_ON_REMAND,
          ReviewScheduleWindow(TODAY.plusMonths(2), TODAY.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner is un-sentenced - review schedule window from 2 to 3 months",
          null,
          ReviewScheduleCalculationRule.PRISONER_UN_SENTENCED,
          ReviewScheduleWindow(TODAY.plusMonths(2), TODAY.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner has between 12 and 60 months left to serve - review schedule window from 4 to 6 months",
          TODAY.plusMonths(48),
          ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(4), TODAY.plusMonths(6)),
        ),
        Arguments.of(
          "prisoner has more than 60 months left to serve - review schedule window from 10 to 12 months",
          TODAY.plusMonths(62),
          ReviewScheduleCalculationRule.MORE_THAN_60_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(10), TODAY.plusMonths(12)),
        ),
        Arguments.of(
          "prisoner has an indeterminate - review schedule window from 10 to 12 months",
          null,
          ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE,
          ReviewScheduleWindow(TODAY.plusMonths(10), TODAY.plusMonths(12)),
        ),
      )

    @JvmStatic
    fun determineReviewScheduleCalculationRule_testCases(): Stream<Arguments> =
      Stream.of(
        Arguments.of(
          "prisoner is a transfer",
          TODAY.plusMonths(24),
          SentenceType.SENTENCED,
          false,
          true,
          ReviewScheduleCalculationRule.PRISONER_TRANSFER,
        ),
        Arguments.of(
          "prisoner is a readmission",
          TODAY.plusMonths(24),
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
          "prisoner has REMAND sentence with 1 day less than 3 months left to serve",
          TODAY.plusMonths(3).minusDays(1),
          SentenceType.REMAND,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is REMAND sentence with exactly 3 months left to serve",
          TODAY.plusMonths(3),
          SentenceType.REMAND,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is REMAND sentence with 3 months and 1 day left to serve",
          TODAY.plusMonths(3).plusDays(1),
          SentenceType.REMAND,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is REMAND sentence with 3 months and 7 days left to serve",
          TODAY.plusMonths(3).plusDays(7),
          SentenceType.REMAND,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is REMAND sentence with 3 months and 8 days left to serve",
          TODAY.plusMonths(3).plusDays(8),
          SentenceType.REMAND,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is REMAND sentence with exactly 6 months left to serve",
          TODAY.plusMonths(6),
          SentenceType.REMAND,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is REMAND sentence with 6 months and 1 day left to serve",
          TODAY.plusMonths(6).plusDays(1),
          SentenceType.REMAND,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is REMAND sentence with exactly 12 months left to serve",
          TODAY.plusMonths(12),
          SentenceType.REMAND,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is REMAND sentence with 12 months and 1 day left to serve",
          TODAY.plusMonths(12).plusDays(1),
          SentenceType.REMAND,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is REMAND sentence with exactly 60 months left to serve",
          TODAY.plusMonths(60),
          SentenceType.REMAND,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is REMAND sentence with more than 60 months left to serve",
          TODAY.plusMonths(60).plusDays(1),
          SentenceType.REMAND,
          false,
          false,
          ReviewScheduleCalculationRule.MORE_THAN_60_MONTHS_TO_SERVE,
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
          "prisoner has CONVICTED_UNSENTENCED sentence with 1 day less than 3 months left to serve",
          TODAY.plusMonths(3).minusDays(1),
          SentenceType.CONVICTED_UNSENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is CONVICTED_UNSENTENCED sentence with exactly 3 months left to serve",
          TODAY.plusMonths(3),
          SentenceType.CONVICTED_UNSENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is CONVICTED_UNSENTENCED sentence with 3 months and 1 day left to serve",
          TODAY.plusMonths(3).plusDays(1),
          SentenceType.CONVICTED_UNSENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is CONVICTED_UNSENTENCED sentence with 3 months and 7 days left to serve",
          TODAY.plusMonths(3).plusDays(7),
          SentenceType.CONVICTED_UNSENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is CONVICTED_UNSENTENCED sentence with 3 months and 8 days left to serve",
          TODAY.plusMonths(3).plusDays(8),
          SentenceType.CONVICTED_UNSENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is CONVICTED_UNSENTENCED sentence with exactly 6 months left to serve",
          TODAY.plusMonths(6),
          SentenceType.CONVICTED_UNSENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is CONVICTED_UNSENTENCED sentence with 6 months and 1 day left to serve",
          TODAY.plusMonths(6).plusDays(1),
          SentenceType.CONVICTED_UNSENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is CONVICTED_UNSENTENCED sentence with exactly 12 months left to serve",
          TODAY.plusMonths(12),
          SentenceType.CONVICTED_UNSENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is CONVICTED_UNSENTENCED sentence with 12 months and 1 day left to serve",
          TODAY.plusMonths(12).plusDays(1),
          SentenceType.CONVICTED_UNSENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is CONVICTED_UNSENTENCED sentence with exactly 60 months left to serve",
          TODAY.plusMonths(60),
          SentenceType.CONVICTED_UNSENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is CONVICTED_UNSENTENCED sentence with more than 60 months left to serve",
          TODAY.plusMonths(60).plusDays(1),
          SentenceType.CONVICTED_UNSENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.MORE_THAN_60_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner has indeterminate sentence with no release date",
          null,
          SentenceType.INDETERMINATE_SENTENCE,
          false,
          false,
          ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE,
        ),
        Arguments.of(
          "prisoner has INTERMEDIATE sentence with 1 day less than 3 months left to serve",
          TODAY.plusMonths(3).minusDays(1),
          SentenceType.INDETERMINATE_SENTENCE,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is INTERMEDIATE sentence with exactly 3 months left to serve",
          TODAY.plusMonths(3),
          SentenceType.INDETERMINATE_SENTENCE,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is INTERMEDIATE sentence with 3 months and 1 day left to serve",
          TODAY.plusMonths(3).plusDays(1),
          SentenceType.INDETERMINATE_SENTENCE,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is INTERMEDIATE sentence with 3 months and 7 days left to serve",
          TODAY.plusMonths(3).plusDays(7),
          SentenceType.INDETERMINATE_SENTENCE,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is INTERMEDIATE sentence with 3 months and 8 days left to serve",
          TODAY.plusMonths(3).plusDays(8),
          SentenceType.INDETERMINATE_SENTENCE,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is INTERMEDIATE sentence with exactly 6 months left to serve",
          TODAY.plusMonths(6),
          SentenceType.INDETERMINATE_SENTENCE,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is INTERMEDIATE sentence with 6 months and 1 day left to serve",
          TODAY.plusMonths(6).plusDays(1),
          SentenceType.INDETERMINATE_SENTENCE,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is INTERMEDIATE sentence with exactly 12 months left to serve",
          TODAY.plusMonths(12),
          SentenceType.INDETERMINATE_SENTENCE,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is INTERMEDIATE sentence with 12 months and 1 day left to serve",
          TODAY.plusMonths(12).plusDays(1),
          SentenceType.INDETERMINATE_SENTENCE,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is INTERMEDIATE sentence with exactly 60 months left to serve",
          TODAY.plusMonths(60),
          SentenceType.INDETERMINATE_SENTENCE,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is INTERMEDIATE sentence with more than 60 months left to serve",
          TODAY.plusMonths(60).plusDays(1),
          SentenceType.INDETERMINATE_SENTENCE,
          false,
          false,
          ReviewScheduleCalculationRule.MORE_THAN_60_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with 1 day less than 3 months left to serve",
          TODAY.plusMonths(3).minusDays(1),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with exactly 3 months left to serve",
          TODAY.plusMonths(3),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with 3 months and 1 day left to serve",
          TODAY.plusMonths(3).plusDays(1),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with 3 months and 7 days left to serve",
          TODAY.plusMonths(3).plusDays(7),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with 3 months and 8 days left to serve",
          TODAY.plusMonths(3).plusDays(8),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with exactly 6 months left to serve",
          TODAY.plusMonths(6),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with 6 months and 1 day left to serve",
          TODAY.plusMonths(6).plusDays(1),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with exactly 12 months left to serve",
          TODAY.plusMonths(12),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with 12 months and 1 day left to serve",
          TODAY.plusMonths(12).plusDays(1),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with exactly 60 months left to serve",
          TODAY.plusMonths(60),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with more than 60 months left to serve",
          TODAY.plusMonths(60).plusDays(1),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.MORE_THAN_60_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner is sentenced with no release date",
          null,
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE,
        ),
        Arguments.of(
          "prisoner is on recall with no release date",
          null,
          SentenceType.RECALL,
          false,
          false,
          ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE,
        ),
        Arguments.of(
          "prisoner is dead with no release date",
          null,
          SentenceType.DEAD,
          false,
          false,
          ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE,
        ),
        Arguments.of(
          "prisoner is civil prisoner with no release date",
          null,
          SentenceType.CIVIL_PRISONER,
          false,
          false,
          ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE,
        ),
        Arguments.of(
          "prisoner is immigration detainee with no release date",
          null,
          SentenceType.IMMIGRATION_DETAINEE,
          false,
          false,
          ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE,
        ),
        Arguments.of(
          "prisoner has unknown sentence type with no release date",
          null,
          SentenceType.UNKNOWN,
          false,
          false,
          ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE,
        ),
        Arguments.of(
          "prisoner has other sentence type with no release date",
          null,
          SentenceType.OTHER,
          false,
          false,
          ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE,
        ),
      )
  }
}
