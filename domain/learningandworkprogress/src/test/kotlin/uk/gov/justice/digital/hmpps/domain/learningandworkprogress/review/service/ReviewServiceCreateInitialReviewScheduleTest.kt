package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ActiveReviewScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.InvalidReviewScheduleException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleWindow
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.aValidCreateInitialReviewScheduleDto
import java.time.LocalDate
import java.util.stream.Stream

@ExtendWith(MockitoExtension::class)
class ReviewServiceCreateInitialReviewScheduleTest {
  @InjectMocks
  private lateinit var service: ReviewService

  @Mock
  private lateinit var reviewEventService: ReviewEventService

  @Mock
  private lateinit var reviewPersistenceAdapter: ReviewPersistenceAdapter

  @Mock
  private lateinit var reviewSchedulePersistenceAdapter: ReviewSchedulePersistenceAdapter

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("prisonersInitialReview_testCases")
  fun `should create initial review schedule`(
    scenario: String,
    releaseDate: LocalDate?,
    sentenceType: SentenceType,
    expectedRule: ReviewScheduleCalculationRule,
    expectedReviewScheduleWindow: ReviewScheduleWindow,
  ) {
    // Given
    val createInitialReviewScheduleDto = aValidCreateInitialReviewScheduleDto(
      prisonNumber = PRISON_NUMBER,
      prisonerReleaseDate = releaseDate,
      prisonerSentenceType = sentenceType,
      isReadmission = false,
      isTransfer = false,
    )

    given(reviewSchedulePersistenceAdapter.getActiveReviewSchedule(any())).willReturn(null)

    val expectedReviewSchedule = aValidReviewSchedule(
      prisonNumber = PRISON_NUMBER,
      earliestReviewDate = expectedReviewScheduleWindow.dateFrom,
      latestReviewDate = expectedReviewScheduleWindow.dateTo,
      scheduleCalculationRule = expectedRule,
    )
    given(reviewSchedulePersistenceAdapter.createReviewSchedule(any())).willReturn(expectedReviewSchedule)

    // When
    val actual = service.createInitialReviewSchedule(createInitialReviewScheduleDto)

    // Then
    assertThat(actual).isEqualTo(expectedReviewSchedule)
    verify(reviewSchedulePersistenceAdapter).getActiveReviewSchedule(PRISON_NUMBER)

    val createReviewScheduleCaptor = argumentCaptor<CreateReviewScheduleDto>()
    verify(reviewSchedulePersistenceAdapter).createReviewSchedule(createReviewScheduleCaptor.capture())
    with(createReviewScheduleCaptor.firstValue) {
      assertThat(prisonNumber).isEqualTo(PRISON_NUMBER)
      assertThat(scheduleCalculationRule).isEqualTo(expectedRule)
      assertThat(reviewScheduleWindow).isEqualTo(expectedReviewScheduleWindow)
    }
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("prisonerWillBeReleasedWithin3Months_testCases")
  fun `should not create initial review schedule given prisoner will be released within 3 months`(
    scenario: String,
    releaseDate: LocalDate?,
    expectedRule: ReviewScheduleCalculationRule,
  ) {
    // Given
    val createInitialReviewScheduleDto = aValidCreateInitialReviewScheduleDto(
      prisonNumber = PRISON_NUMBER,
      prisonerReleaseDate = releaseDate,
      prisonerSentenceType = SentenceType.SENTENCED,
      isReadmission = false,
      isTransfer = false,
    )

    given(reviewSchedulePersistenceAdapter.getActiveReviewSchedule(any())).willReturn(null)

    // When
    val actual = service.createInitialReviewSchedule(createInitialReviewScheduleDto)

    // Then
    assertThat(actual).isNull()
    verify(reviewSchedulePersistenceAdapter).getActiveReviewSchedule(PRISON_NUMBER)
    verifyNoMoreInteractions(reviewSchedulePersistenceAdapter)
  }

  @Test
  fun `should create initial review schedule given prisoner has been transferred`() {
    // Given
    val createInitialReviewScheduleDto = aValidCreateInitialReviewScheduleDto(
      prisonNumber = PRISON_NUMBER,
      prisonerReleaseDate = TODAY.plusYears(1),
      prisonerSentenceType = SentenceType.SENTENCED,
      isTransfer = true,
      isReadmission = false,
    )

    given(reviewSchedulePersistenceAdapter.getActiveReviewSchedule(any())).willReturn(null)

    val expectedReviewSchedule = aValidReviewSchedule(
      prisonNumber = PRISON_NUMBER,
      earliestReviewDate = TODAY,
      latestReviewDate = TODAY.plusDays(10),
      scheduleCalculationRule = ReviewScheduleCalculationRule.PRISONER_TRANSFER,
    )
    given(reviewSchedulePersistenceAdapter.createReviewSchedule(any())).willReturn(expectedReviewSchedule)

    // When
    val actual = service.createInitialReviewSchedule(createInitialReviewScheduleDto)

    // Then
    assertThat(actual).isEqualTo(expectedReviewSchedule)
    verify(reviewSchedulePersistenceAdapter).getActiveReviewSchedule(PRISON_NUMBER)

    val createReviewScheduleCaptor = argumentCaptor<CreateReviewScheduleDto>()
    verify(reviewSchedulePersistenceAdapter).createReviewSchedule(createReviewScheduleCaptor.capture())
    with(createReviewScheduleCaptor.firstValue) {
      assertThat(prisonNumber).isEqualTo(PRISON_NUMBER)
      assertThat(scheduleCalculationRule).isEqualTo(ReviewScheduleCalculationRule.PRISONER_TRANSFER)
      assertThat(reviewScheduleWindow).isEqualTo(ReviewScheduleWindow(TODAY, TODAY.plusDays(10)))
    }
  }

  @Test
  fun `should create initial review schedule given prisoner has been re-admitted`() {
    // Given
    val createInitialReviewScheduleDto = aValidCreateInitialReviewScheduleDto(
      prisonNumber = PRISON_NUMBER,
      prisonerReleaseDate = TODAY.plusYears(1),
      prisonerSentenceType = SentenceType.SENTENCED,
      isTransfer = false,
      isReadmission = true,
    )

    given(reviewSchedulePersistenceAdapter.getActiveReviewSchedule(any())).willReturn(null)

    val expectedReviewSchedule = aValidReviewSchedule(
      prisonNumber = PRISON_NUMBER,
      earliestReviewDate = TODAY,
      latestReviewDate = TODAY.plusDays(10),
      scheduleCalculationRule = ReviewScheduleCalculationRule.PRISONER_READMISSION,
    )
    given(reviewSchedulePersistenceAdapter.createReviewSchedule(any())).willReturn(expectedReviewSchedule)

    // When
    val actual = service.createInitialReviewSchedule(createInitialReviewScheduleDto)

    // Then
    assertThat(actual).isEqualTo(expectedReviewSchedule)
    verify(reviewSchedulePersistenceAdapter).getActiveReviewSchedule(PRISON_NUMBER)

    val createReviewScheduleCaptor = argumentCaptor<CreateReviewScheduleDto>()
    verify(reviewSchedulePersistenceAdapter).createReviewSchedule(createReviewScheduleCaptor.capture())
    with(createReviewScheduleCaptor.firstValue) {
      assertThat(prisonNumber).isEqualTo(PRISON_NUMBER)
      assertThat(scheduleCalculationRule).isEqualTo(ReviewScheduleCalculationRule.PRISONER_READMISSION)
      assertThat(reviewScheduleWindow).isEqualTo(ReviewScheduleWindow(TODAY, TODAY.plusDays(10)))
    }
  }

  @Test
  fun `should not create initial review schedule given prisoner already has an active review schedule`() {
    // Given
    val createInitialReviewScheduleDto = aValidCreateInitialReviewScheduleDto(
      prisonNumber = PRISON_NUMBER,
      prisonerReleaseDate = TODAY.plusYears(1),
      prisonerSentenceType = SentenceType.SENTENCED,
    )

    val reviewSchedule = aValidReviewSchedule(prisonNumber = PRISON_NUMBER)
    given(reviewSchedulePersistenceAdapter.getActiveReviewSchedule(any())).willReturn(reviewSchedule)

    // When
    val exception = catchThrowableOfType(ActiveReviewScheduleAlreadyExistsException::class.java) {
      service.createInitialReviewSchedule(createInitialReviewScheduleDto)
    }

    // Then
    assertThat(exception.prisonNumber).isEqualTo(PRISON_NUMBER)
    verify(reviewSchedulePersistenceAdapter).getActiveReviewSchedule(PRISON_NUMBER)
    verifyNoMoreInteractions(reviewSchedulePersistenceAdapter)
  }

  @ParameterizedTest
  @CsvSource(value = ["DEAD", "CIVIL_PRISONER", "IMMIGRATION_DETAINEE", "UNKNOWN", "OTHER"])
  fun `should not create a review given prisoner has sentence type`(sentenceType: SentenceType) {
    // Given
    val createInitialReviewScheduleDto = aValidCreateInitialReviewScheduleDto(
      prisonNumber = PRISON_NUMBER,
      prisonerReleaseDate = TODAY.plusYears(1),
      prisonerSentenceType = sentenceType,
    )

    // When
    val exception = catchThrowableOfType(InvalidReviewScheduleException::class.java) {
      service.createInitialReviewSchedule(createInitialReviewScheduleDto)
    }

    // Then
    assertThat(exception)
      .isInstanceOf(InvalidReviewScheduleException.ReviewScheduleInvalidSentenceTypeException::class.java)
      .extracting("prisonNumber", "sentenceType")
      .containsExactly(PRISON_NUMBER, sentenceType)
  }

  @ParameterizedTest
  @CsvSource(value = ["SENTENCED", "RECALL"])
  fun `should not create a review given prisoner with no release date has sentence type`(sentenceType: SentenceType) {
    // Given
    val createInitialReviewScheduleDto = aValidCreateInitialReviewScheduleDto(
      prisonNumber = PRISON_NUMBER,
      prisonerReleaseDate = null,
      prisonerSentenceType = sentenceType,
    )

    // When
    val exception = catchThrowableOfType(InvalidReviewScheduleException::class.java) {
      service.createInitialReviewSchedule(createInitialReviewScheduleDto)
    }

    // Then
    assertThat(exception)
      .isInstanceOf(InvalidReviewScheduleException.ReviewScheduleNoReleaseDateForSentenceTypeException::class.java)
      .extracting("prisonNumber", "sentenceType")
      .containsExactly(PRISON_NUMBER, sentenceType)
  }

  companion object {
    private const val PRISON_NUMBER = "A1234AB"
    private val TODAY = LocalDate.now()

    @JvmStatic
    fun prisonersInitialReview_testCases(): Stream<Arguments> =
      Stream.of(
        Arguments.of(
          "prisoner has an indeterminate sentence - review scheduled for 10 to 12 months",
          null,
          SentenceType.INDETERMINATE_SENTENCE,
          ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE,
          ReviewScheduleWindow(TODAY.plusMonths(10), TODAY.plusMonths(12)),
        ),
        Arguments.of(
          "prisoner is on remand - review scheduled for 2 to 3 months",
          null,
          SentenceType.REMAND,
          ReviewScheduleCalculationRule.PRISONER_ON_REMAND,
          ReviewScheduleWindow(TODAY.plusMonths(2), TODAY.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner is un-sentenced - review scheduled for 2 to 3 months",
          null,
          SentenceType.CONVICTED_UNSENTENCED,
          ReviewScheduleCalculationRule.PRISONER_UN_SENTENCED,
          ReviewScheduleWindow(TODAY.plusMonths(2), TODAY.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner has 3 months 1 day left to serve - review scheduled for 1 to 3 months minus 7 days",
          TODAY.plusMonths(3).plusDays(1),
          SentenceType.SENTENCED,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(1), TODAY.plusMonths(3).minusDays(7)),
        ),
        Arguments.of(
          "prisoner has 3 months 7 days left to serve - review scheduled for 1 to 3 months minus 1 day",
          TODAY.plusMonths(3).plusDays(7),
          SentenceType.SENTENCED,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(1), TODAY.plusMonths(3).minusDays(1)),
        ),
        Arguments.of(
          "prisoner has between 3 months 8 days and 6 months left to serve - review scheduled for 1 to 3 months",
          TODAY.plusMonths(3).plusDays(8),
          SentenceType.SENTENCED,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(1), TODAY.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner has exactly 6 months left to serve - review scheduled for 1 to 3 months",
          TODAY.plusMonths(6),
          SentenceType.SENTENCED,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(1), TODAY.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner has between 6 and 12 months left to serve - review scheduled for 2 to 3 months",
          TODAY.plusMonths(6).plusDays(1),
          SentenceType.SENTENCED,
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(2), TODAY.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner has exactly 12 months left to serve - review scheduled for 2 to 3 months",
          TODAY.plusMonths(12),
          SentenceType.SENTENCED,
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(2), TODAY.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner has between 12 and 60 months left to serve - review scheduled for 4 to 6 months",
          TODAY.plusMonths(12).plusDays(1),
          SentenceType.SENTENCED,
          ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(4), TODAY.plusMonths(6)),
        ),
        Arguments.of(
          "prisoner has exactly 60 months left to serve - review scheduled for 4 to 6 months",
          TODAY.plusMonths(60),
          SentenceType.SENTENCED,
          ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(4), TODAY.plusMonths(6)),
        ),
        Arguments.of(
          "prisoner has more than 60 months left to serve - review scheduled for 10 to 12 months",
          TODAY.plusMonths(60).plusDays(1),
          SentenceType.SENTENCED,
          ReviewScheduleCalculationRule.MORE_THAN_60_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(10), TODAY.plusMonths(12)),
        ),
      )

    @JvmStatic
    fun prisonerWillBeReleasedWithin3Months_testCases(): Stream<Arguments> =
      Stream.of(
        Arguments.of(
          "prisoner has less than 3 months left to serve - no review schedule",
          TODAY.plusMonths(3).minusDays(1),
          ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner has exactly 3 months left to serve - no review schedule",
          TODAY.plusMonths(3),
          ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE,
        ),
      )
  }
}
