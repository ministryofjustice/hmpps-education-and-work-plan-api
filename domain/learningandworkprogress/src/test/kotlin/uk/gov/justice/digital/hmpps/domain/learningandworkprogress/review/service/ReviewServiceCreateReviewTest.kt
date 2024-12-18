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
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNoReleaseDateForSentenceTypeException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleWindow
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidCompletedReview
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CompletedReviewDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateCompletedReviewDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.CreateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.UpdateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.aValidCreateCompletedReviewDto
import java.time.LocalDate
import java.util.stream.Stream

@ExtendWith(MockitoExtension::class)
class ReviewServiceCreateReviewTest {
  @InjectMocks
  private lateinit var service: ReviewService

  @Mock
  private lateinit var reviewEventService: ReviewEventService

  @Mock
  private lateinit var reviewPersistenceAdapter: ReviewPersistenceAdapter

  @Mock
  private lateinit var reviewSchedulePersistenceAdapter: ReviewSchedulePersistenceAdapter

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("notPrisonersLastReview_testCases")
  fun `should create a review and create a new review schedule given this is not the prisoner's last review`(
    scenario: String,
    releaseDate: LocalDate?,
    sentenceType: SentenceType,
    prisonerHasIndeterminateFlag: Boolean,
    prisonerHasRecallFlag: Boolean,
    expectedRule: ReviewScheduleCalculationRule,
    expectedReviewScheduleWindow: ReviewScheduleWindow,
  ) {
    // Given
    val createCompletedReviewDto = aValidCreateCompletedReviewDto(
      prisonNumber = PRISON_NUMBER,
      conductedAt = TODAY,
      prisonerReleaseDate = releaseDate,
      prisonerSentenceType = sentenceType,
      prisonerHasIndeterminateFlag = prisonerHasIndeterminateFlag,
      prisonerHasRecallFlag = prisonerHasRecallFlag,
    )

    val activeReviewSchedule = aValidReviewSchedule(
      latestReviewDate = TODAY.plusDays(10),
    )
    given(reviewSchedulePersistenceAdapter.getActiveReviewSchedule(any())).willReturn(activeReviewSchedule)
    val updatedReviewSchedule = activeReviewSchedule.copy(scheduleStatus = ReviewScheduleStatus.COMPLETED)
    given(reviewSchedulePersistenceAdapter.updateReviewSchedule(any())).willReturn(updatedReviewSchedule)

    val completedReview = aValidCompletedReview()
    given(reviewPersistenceAdapter.createCompletedReview(any(), any())).willReturn(completedReview)

    val expectedNewReviewSchedule = aValidReviewSchedule()
    given(reviewSchedulePersistenceAdapter.createReviewSchedule(any())).willReturn(expectedNewReviewSchedule)

    val expected = CompletedReviewDto(
      completedReview = completedReview,
      wasLastReviewBeforeRelease = false,
      latestReviewSchedule = expectedNewReviewSchedule,
    )

    // When
    val actual = service.createReview(createCompletedReviewDto)

    // Then
    assertThat(actual).isEqualTo(expected)
    verify(reviewSchedulePersistenceAdapter).getActiveReviewSchedule(PRISON_NUMBER)

    val updateReviewScheduleCaptor = argumentCaptor<UpdateReviewScheduleDto>()
    verify(reviewSchedulePersistenceAdapter).updateReviewSchedule(updateReviewScheduleCaptor.capture())
    with(updateReviewScheduleCaptor.firstValue) {
      assertThat(scheduleStatus).isEqualTo(ReviewScheduleStatus.COMPLETED)
    }

    val createCompletedReviewCaptor = argumentCaptor<CreateCompletedReviewDto>()
    verify(reviewPersistenceAdapter).createCompletedReview(createCompletedReviewCaptor.capture(), eq(activeReviewSchedule))
    with(createCompletedReviewCaptor.firstValue) {
      assertThat(prisonNumber).isEqualTo(PRISON_NUMBER)
      assertThat(conductedAt).isEqualTo(createCompletedReviewDto.conductedAt)
      assertThat(conductedBy).isEqualTo(createCompletedReviewDto.conductedBy)
      assertThat(conductedByRole).isEqualTo(createCompletedReviewDto.conductedByRole)
      assertThat(prisonerReleaseDate).isEqualTo(prisonerReleaseDate)
      assertThat(prisonerSentenceType).isEqualTo(sentenceType)
    }

    val createReviewScheduleCaptor = argumentCaptor<CreateReviewScheduleDto>()
    verify(reviewSchedulePersistenceAdapter).createReviewSchedule(createReviewScheduleCaptor.capture())
    with(createReviewScheduleCaptor.firstValue) {
      assertThat(scheduleCalculationRule).isEqualTo(expectedRule)
      assertThat(reviewScheduleWindow).isEqualTo(expectedReviewScheduleWindow)
    }

    verify(reviewEventService).reviewCompleted(completedReview)
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("prisonersLastReview_testCases")
  fun `should create a review and not create a new review schedule given this is the prisoner's last review`(
    scenario: String,
    releaseDate: LocalDate?,
    expectedRule: ReviewScheduleCalculationRule,
  ) {
    // Given
    val createCompletedReviewDto = aValidCreateCompletedReviewDto(
      prisonNumber = PRISON_NUMBER,
      conductedAt = TODAY,
      prisonerReleaseDate = releaseDate,
      prisonerSentenceType = SentenceType.SENTENCED,
      prisonerHasIndeterminateFlag = false,
      prisonerHasRecallFlag = false,
    )

    val activeReviewSchedule = aValidReviewSchedule(
      latestReviewDate = TODAY.plusDays(10),
    )
    given(reviewSchedulePersistenceAdapter.getActiveReviewSchedule(any())).willReturn(activeReviewSchedule)
    val updatedReviewSchedule = activeReviewSchedule.copy(scheduleStatus = ReviewScheduleStatus.COMPLETED)
    given(reviewSchedulePersistenceAdapter.updateReviewSchedule(any())).willReturn(updatedReviewSchedule)

    val completedReview = aValidCompletedReview()
    given(reviewPersistenceAdapter.createCompletedReview(any(), any())).willReturn(completedReview)

    val expected = CompletedReviewDto(
      completedReview = completedReview,
      wasLastReviewBeforeRelease = true,
      latestReviewSchedule = updatedReviewSchedule,
    )

    // When
    val actual = service.createReview(createCompletedReviewDto)

    // Then
    assertThat(actual).isEqualTo(expected)
    verify(reviewSchedulePersistenceAdapter).getActiveReviewSchedule(PRISON_NUMBER)
    verifyNoMoreInteractions(reviewSchedulePersistenceAdapter)

    val updateReviewScheduleCaptor = argumentCaptor<UpdateReviewScheduleDto>()
    verify(reviewSchedulePersistenceAdapter).updateReviewSchedule(updateReviewScheduleCaptor.capture())
    with(updateReviewScheduleCaptor.firstValue) {
      assertThat(scheduleStatus).isEqualTo(ReviewScheduleStatus.COMPLETED)
    }

    val createCompletedReviewCaptor = argumentCaptor<CreateCompletedReviewDto>()
    verify(reviewPersistenceAdapter).createCompletedReview(createCompletedReviewCaptor.capture(), eq(activeReviewSchedule))
    with(createCompletedReviewCaptor.firstValue) {
      assertThat(prisonNumber).isEqualTo(PRISON_NUMBER)
      assertThat(conductedAt).isEqualTo(createCompletedReviewDto.conductedAt)
      assertThat(conductedBy).isEqualTo(createCompletedReviewDto.conductedBy)
      assertThat(conductedByRole).isEqualTo(createCompletedReviewDto.conductedByRole)
      assertThat(prisonerReleaseDate).isEqualTo(prisonerReleaseDate)
      assertThat(prisonerSentenceType).isEqualTo(SentenceType.SENTENCED)
    }

    verify(reviewSchedulePersistenceAdapter, never()).createReviewSchedule(any())
    verify(reviewEventService).reviewCompleted(completedReview)
  }

  @Test
  fun `should not create a review given prisoner does not have an active review schedule`() {
    // Given
    val createCompletedReviewDto = aValidCreateCompletedReviewDto(
      prisonNumber = PRISON_NUMBER,
      conductedAt = TODAY,
      prisonerReleaseDate = TODAY.plusYears(1),
      prisonerSentenceType = SentenceType.SENTENCED,
    )

    given(reviewSchedulePersistenceAdapter.getActiveReviewSchedule(any())).willReturn(null)

    // When
    val exception = catchThrowableOfType(ReviewScheduleNotFoundException::class.java) {
      service.createReview(createCompletedReviewDto)
    }

    // Then
    assertThat(exception.prisonNumber).isEqualTo(PRISON_NUMBER)
    verify(reviewSchedulePersistenceAdapter).getActiveReviewSchedule(PRISON_NUMBER)
    verifyNoMoreInteractions(reviewSchedulePersistenceAdapter)
    verifyNoInteractions(reviewPersistenceAdapter)
    verifyNoInteractions(reviewEventService)
  }

  @ParameterizedTest
  @CsvSource(value = ["SENTENCED", "RECALL", "CIVIL_PRISONER", "IMMIGRATION_DETAINEE", "UNKNOWN", "OTHER"])
  fun `should not create a review given prisoner with no release date has sentence type`(sentenceType: SentenceType) {
    // Given
    val createCompletedReviewDto = aValidCreateCompletedReviewDto(
      prisonNumber = PRISON_NUMBER,
      conductedAt = TODAY,
      prisonerReleaseDate = null,
      prisonerSentenceType = sentenceType,
      prisonerHasIndeterminateFlag = false,
      prisonerHasRecallFlag = false,
    )

    // When
    val exception = catchThrowableOfType(ReviewScheduleNoReleaseDateForSentenceTypeException::class.java) {
      service.createReview(createCompletedReviewDto)
    }

    // Then
    assertThat(exception)
      .isInstanceOf(ReviewScheduleNoReleaseDateForSentenceTypeException::class.java)
      .extracting("prisonNumber", "sentenceType")
      .containsExactly(PRISON_NUMBER, sentenceType)
  }

  companion object {
    private const val PRISON_NUMBER = "A1234AB"
    private val TODAY = LocalDate.now()

    @JvmStatic
    fun notPrisonersLastReview_testCases(): Stream<Arguments> =
      Stream.of(
        Arguments.of(
          "prisoner has an indeterminate sentence - next review 10 to 12 months",
          null,
          SentenceType.INDETERMINATE_SENTENCE,
          true,
          false,
          ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE,
          ReviewScheduleWindow(TODAY.plusMonths(10), TODAY.plusMonths(12)),
        ),
        Arguments.of(
          "prisoner is on remand - next review 2 to 3 months",
          null,
          SentenceType.REMAND,
          false,
          false,
          ReviewScheduleCalculationRule.PRISONER_ON_REMAND,
          ReviewScheduleWindow(TODAY.plusMonths(2), TODAY.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner is un-sentenced - next review 2 to 3 months",
          null,
          SentenceType.CONVICTED_UNSENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.PRISONER_UN_SENTENCED,
          ReviewScheduleWindow(TODAY.plusMonths(2), TODAY.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner has 3 months 1 day left to serve - next review 1 to 3 months minus 7 days",
          TODAY.plusMonths(3).plusDays(1),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(1), TODAY.plusMonths(3).minusDays(7)),
        ),
        Arguments.of(
          "prisoner has 3 months 7 days left to serve - next review 1 to 3 months minus 1 day",
          TODAY.plusMonths(3).plusDays(7),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_AND_3_MONTHS_7_DAYS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(1), TODAY.plusMonths(3).minusDays(1)),
        ),
        Arguments.of(
          "prisoner has between 3 months 8 days and 6 months left to serve - next review 1 to 3 months",
          TODAY.plusMonths(3).plusDays(8),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(1), TODAY.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner has exactly 6 months left to serve - next review 1 to 3 months",
          TODAY.plusMonths(6),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_3_MONTHS_8_DAYS_AND_6_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(1), TODAY.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner has between 6 and 12 months left to serve - next review 2 to 3 months",
          TODAY.plusMonths(6).plusDays(1),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(2), TODAY.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner has exactly 12 months left to serve - next review 2 to 3 months",
          TODAY.plusMonths(12),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(2), TODAY.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner has between 12 and 60 months left to serve - next review 4 to 6 months",
          TODAY.plusMonths(12).plusDays(1),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(4), TODAY.plusMonths(6)),
        ),
        Arguments.of(
          "prisoner has exactly 60 months left to serve - next review 4 to 6 months",
          TODAY.plusMonths(60),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(4), TODAY.plusMonths(6)),
        ),
        Arguments.of(
          "prisoner has more than 60 months left to serve - next review 10 to 12 months",
          TODAY.plusMonths(60).plusDays(1),
          SentenceType.SENTENCED,
          false,
          false,
          ReviewScheduleCalculationRule.MORE_THAN_60_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(10), TODAY.plusMonths(12)),
        ),
        Arguments.of(
          "prisoner is sentenced with a release date, but has the indeterminate flag set - review scheduled for 10 to 12 months",
          TODAY.plusMonths(60).plusDays(1),
          SentenceType.SENTENCED,
          true,
          false,
          ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE,
          ReviewScheduleWindow(TODAY.plusMonths(10), TODAY.plusMonths(12)),
        ),
        Arguments.of(
          "prisoner is sentenced without a release date, but has the indeterminate flag set - review scheduled for 10 to 12 months",
          null,
          SentenceType.SENTENCED,
          true,
          false,
          ReviewScheduleCalculationRule.INDETERMINATE_SENTENCE,
          ReviewScheduleWindow(TODAY.plusMonths(10), TODAY.plusMonths(12)),
        ),
        Arguments.of(
          "prisoner with CIVIL_PRISONER sentence has between 6 and 12 months left to serve - next review 2 to 3 months",
          TODAY.plusMonths(6).plusDays(1),
          SentenceType.CIVIL_PRISONER,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(2), TODAY.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner with IMMIGRATION_DETAINEE sentence has between 6 and 12 months left to serve - next review 2 to 3 months",
          TODAY.plusMonths(6).plusDays(1),
          SentenceType.IMMIGRATION_DETAINEE,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(2), TODAY.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner with UNKNOWN sentence has between 6 and 12 months left to serve - next review 2 to 3 months",
          TODAY.plusMonths(6).plusDays(1),
          SentenceType.UNKNOWN,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(2), TODAY.plusMonths(3)),
        ),
        Arguments.of(
          "prisoner with OTHER sentence has between 6 and 12 months left to serve - next review 2 to 3 months",
          TODAY.plusMonths(6).plusDays(1),
          SentenceType.OTHER,
          false,
          false,
          ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
          ReviewScheduleWindow(TODAY.plusMonths(2), TODAY.plusMonths(3)),
        ),
      )

    @JvmStatic
    fun prisonersLastReview_testCases(): Stream<Arguments> =
      Stream.of(
        Arguments.of(
          "prisoner has less than 3 months left to serve - no next review",
          TODAY.plusMonths(3).minusDays(1),
          ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE,
        ),
        Arguments.of(
          "prisoner has exactly 3 months left to serve - no next review",
          TODAY.plusMonths(3),
          ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE,
        ),
      )
  }
}
