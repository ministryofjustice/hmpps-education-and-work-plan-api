package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule
import java.time.LocalDate
import java.util.stream.Stream

@ExtendWith(MockitoExtension::class)
class ReviewServiceDetermineReviewScheduleCalculationRuleTest {

  @InjectMocks
  private lateinit var service: ReviewService

  @Mock
  private lateinit var reviewPersistenceAdapter: ReviewPersistenceAdapter

  @Mock
  private lateinit var reviewSchedulePersistenceAdapter: ReviewSchedulePersistenceAdapter

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("testCases")
  fun `it should determine review schedule calculation rule given prisoner scenario`(
    scenario: String,
    releaseDate: LocalDate?,
    sentenceType: SentenceType,
    isReAdmission: Boolean,
    isTransfer: Boolean,
    expectedRule: ReviewScheduleCalculationRule,
  ) {
    // Given

    // When
    val actual = service.determineReviewScheduleCalculationRule(releaseDate, sentenceType, isReAdmission, isTransfer)

    // Then
    assertThat(actual).isEqualTo(expectedRule)
  }

  companion object {
    private const val PRISON_NUMBER = "A1234AB"
    private val TODAY = LocalDate.now()

    @JvmStatic
    fun testCases(): Stream<Arguments> =
      Stream.of(
        Arguments.of(
          "prisoner has been re-admitted",
          LocalDate.now().plusYears(2),
          SentenceType.SENTENCED,
          true,
          false,
          ReviewScheduleCalculationRule.PRISONER_READMISSION,
        ),
        Arguments.of(
          "prisoner has been transferred",
          LocalDate.now().plusYears(2),
          SentenceType.SENTENCED,
          false,
          true,
          ReviewScheduleCalculationRule.PRISONER_TRANSFER,
        ),
        Arguments.of(
          "prisoner has an indeterminate sentence",
          null,
          SentenceType.INDETERMINATE_SENTENCE,
          false,
          false,
          ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE,
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
          "prisoner is un-sentenced",
          null,
          SentenceType.CONVICTED_UNSENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.PRISONER_UN_SENTENCED,
        ),
        Arguments.of(
          "prisoner has less than 6 months left to serve",
          TODAY.plusMonths(6).minusDays(1),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.LESS_THAN_6_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner has exactly 6 months left to serve",
          TODAY.plusMonths(6),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.LESS_THAN_6_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner has between 6 and 12 months left to serve",
          TODAY.plusMonths(6).plusDays(1),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner has exactly 12 months left to serve",
          TODAY.plusMonths(12),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner has between 12 and 60 months left to serve",
          TODAY.plusMonths(12).plusDays(1),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner has exactly 60 months left to serve",
          TODAY.plusMonths(60),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner has more than 60 months left to serve",
          TODAY.plusMonths(60).plusDays(1),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.MORE_THAN_60_MONTHS_TO_SERVE,
        ),
      )
  }
}
