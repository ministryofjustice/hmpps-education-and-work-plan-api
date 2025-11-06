package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
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
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import java.time.LocalDate
import java.util.stream.Stream

@ExtendWith(MockitoExtension::class)
class ReviewServiceTest {
  @InjectMocks
  private lateinit var service: ReviewService

  @Mock
  private lateinit var reviewEventService: ReviewEventService

  @Mock
  private lateinit var reviewPersistenceAdapter: ReviewPersistenceAdapter

  @Mock
  private lateinit var reviewSchedulePersistenceAdapter: ReviewSchedulePersistenceAdapter

  @Mock
  private lateinit var reviewScheduleService: ReviewScheduleService

  @Mock
  private lateinit var reviewScheduleDateCalculationService: ReviewScheduleDateCalculationService

  @Nested
  inner class GetCompletedReviewsForPrisoner {
    @Test
    fun `should get completed reviews for a prisoner`() {
      // Given
      val expected = listOf(aValidCompletedReview())
      given(reviewPersistenceAdapter.getCompletedReviews(any())).willReturn(expected)

      // When
      val actual = service.getCompletedReviewsForPrisoner(PRISON_NUMBER)

      // Then
      assertThat(actual).isEqualTo(expected)
      verify(reviewPersistenceAdapter).getCompletedReviews(PRISON_NUMBER)
    }
  }

  @Nested
  inner class CreateReview {
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewServiceTest#notPrisonersLastReview_testCases")
    fun `should create a review and create a new review schedule given this is not the prisoner's last review before release`(
      scenario: String,
      releaseDate: LocalDate?,
      effectiveSentenceType: SentenceType,
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
        prisonerSentenceType = SentenceType.SENTENCED,
        prisonerHasIndeterminateFlag = prisonerHasIndeterminateFlag,
        prisonerHasRecallFlag = prisonerHasRecallFlag,
      )

      val activeReviewSchedule = aValidReviewSchedule(
        latestReviewDate = TODAY.plusDays(10),
      )
      given(reviewScheduleService.getActiveReviewScheduleForPrisoner(any())).willReturn(activeReviewSchedule)
      val updatedReviewSchedule = activeReviewSchedule.copy(scheduleStatus = ReviewScheduleStatus.COMPLETED)
      given(reviewSchedulePersistenceAdapter.updateReviewSchedule(any())).willReturn(updatedReviewSchedule)

      val completedReview = aValidCompletedReview()
      given(reviewPersistenceAdapter.createCompletedReview(any(), any())).willReturn(completedReview)

      val expectedNewReviewSchedule = aValidReviewSchedule()
      given(reviewSchedulePersistenceAdapter.createReviewSchedule(any())).willReturn(expectedNewReviewSchedule)

      given(
        reviewScheduleDateCalculationService.determineReviewScheduleCalculationRule(
          any(),
          any(),
          anyOrNull(),
          any(),
          any(),
        ),
      )
        .willReturn(expectedRule)
      given(reviewScheduleDateCalculationService.calculateReviewWindow(any(), anyOrNull()))
        .willReturn(expectedReviewScheduleWindow)

      val expected = CompletedReviewDto(
        completedReview = completedReview,
        wasLastReviewBeforeRelease = false,
        latestReviewSchedule = expectedNewReviewSchedule,
      )

      // When
      val actual = service.createReview(createCompletedReviewDto)

      // Then
      assertThat(actual).isEqualTo(expected)
      verify(reviewScheduleService).getActiveReviewScheduleForPrisoner(PRISON_NUMBER)

      verify(reviewScheduleDateCalculationService).determineReviewScheduleCalculationRule(
        PRISON_NUMBER,
        effectiveSentenceType,
        releaseDate,
        false,
        false,
      )
      verify(reviewScheduleDateCalculationService).calculateReviewWindow(expectedRule, releaseDate)

      val updateReviewScheduleCaptor = argumentCaptor<UpdateReviewScheduleDto>()
      verify(reviewSchedulePersistenceAdapter).updateReviewSchedule(updateReviewScheduleCaptor.capture())
      with(updateReviewScheduleCaptor.firstValue) {
        assertThat(scheduleStatus).isEqualTo(ReviewScheduleStatus.COMPLETED)
      }

      val createCompletedReviewCaptor = argumentCaptor<CreateCompletedReviewDto>()
      verify(reviewPersistenceAdapter).createCompletedReview(
        createCompletedReviewCaptor.capture(),
        eq(activeReviewSchedule),
      )
      with(createCompletedReviewCaptor.firstValue) {
        assertThat(prisonNumber).isEqualTo(PRISON_NUMBER)
        assertThat(conductedAt).isEqualTo(createCompletedReviewDto.conductedAt)
        assertThat(conductedBy).isEqualTo(createCompletedReviewDto.conductedBy)
        assertThat(conductedByRole).isEqualTo(createCompletedReviewDto.conductedByRole)
        assertThat(prisonerReleaseDate).isEqualTo(prisonerReleaseDate)
        assertThat(prisonerSentenceType).isEqualTo(SentenceType.SENTENCED)
      }

      val createReviewScheduleCaptor = argumentCaptor<CreateReviewScheduleDto>()
      verify(reviewSchedulePersistenceAdapter).createReviewSchedule(createReviewScheduleCaptor.capture())
      with(createReviewScheduleCaptor.firstValue) {
        assertThat(scheduleCalculationRule).isEqualTo(expectedRule)
        assertThat(reviewScheduleWindow).isEqualTo(expectedReviewScheduleWindow)
      }

      verify(reviewPersistenceAdapter, never()).markCompletedReviewAsThePrisonersPreReleaseReview(any())

      verify(reviewEventService).reviewCompleted(completedReview)
    }

    @Test
    fun `should create a review and not create a new review schedule given this is the prisoner's last review before release`() {
      // Given
      val releaseDate = TODAY.plusMonths(3).minusDays(1)
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
      given(reviewScheduleService.getActiveReviewScheduleForPrisoner(any())).willReturn(activeReviewSchedule)
      val updatedReviewSchedule = activeReviewSchedule.copy(scheduleStatus = ReviewScheduleStatus.COMPLETED)
      given(reviewSchedulePersistenceAdapter.updateReviewSchedule(any())).willReturn(updatedReviewSchedule)

      val completedReview = aValidCompletedReview()
      given(reviewPersistenceAdapter.createCompletedReview(any(), any())).willReturn(completedReview)

      given(
        reviewScheduleDateCalculationService.determineReviewScheduleCalculationRule(
          any(),
          any(),
          anyOrNull(),
          any(),
          any(),
        ),
      )
        .willReturn(ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE)
      given(reviewScheduleDateCalculationService.calculateReviewWindow(any(), anyOrNull()))
        .willReturn(null)

      val expected = CompletedReviewDto(
        completedReview = completedReview,
        wasLastReviewBeforeRelease = true,
        latestReviewSchedule = updatedReviewSchedule,
      )

      // When
      val actual = service.createReview(createCompletedReviewDto)

      // Then
      assertThat(actual).isEqualTo(expected)
      verify(reviewScheduleService).getActiveReviewScheduleForPrisoner(PRISON_NUMBER)

      verify(reviewScheduleDateCalculationService).determineReviewScheduleCalculationRule(
        PRISON_NUMBER,
        SentenceType.SENTENCED,
        releaseDate,
        false,
        false,
      )
      verify(reviewScheduleDateCalculationService).calculateReviewWindow(
        ReviewScheduleCalculationRule.BETWEEN_RELEASE_AND_3_MONTHS_TO_SERVE,
        releaseDate,
      )

      val updateReviewScheduleCaptor = argumentCaptor<UpdateReviewScheduleDto>()
      verify(reviewSchedulePersistenceAdapter).updateReviewSchedule(updateReviewScheduleCaptor.capture())
      with(updateReviewScheduleCaptor.firstValue) {
        assertThat(scheduleStatus).isEqualTo(ReviewScheduleStatus.COMPLETED)
      }

      val createCompletedReviewCaptor = argumentCaptor<CreateCompletedReviewDto>()
      verify(reviewPersistenceAdapter).createCompletedReview(
        createCompletedReviewCaptor.capture(),
        eq(activeReviewSchedule),
      )
      with(createCompletedReviewCaptor.firstValue) {
        assertThat(prisonNumber).isEqualTo(PRISON_NUMBER)
        assertThat(conductedAt).isEqualTo(createCompletedReviewDto.conductedAt)
        assertThat(conductedBy).isEqualTo(createCompletedReviewDto.conductedBy)
        assertThat(conductedByRole).isEqualTo(createCompletedReviewDto.conductedByRole)
        assertThat(prisonerReleaseDate).isEqualTo(prisonerReleaseDate)
        assertThat(prisonerSentenceType).isEqualTo(SentenceType.SENTENCED)
      }

      verify(reviewSchedulePersistenceAdapter, never()).createReviewSchedule(any())

      verify(reviewPersistenceAdapter).markCompletedReviewAsThePrisonersPreReleaseReview(completedReview.reference)

      verify(reviewEventService).reviewCompleted(completedReview)
    }

    @Test
    fun `should not create a review given reviewScheduleDateCalculationService throws ReviewScheduleNoReleaseDateForSentenceTypeException`() {
      // Given
      val releaseDate = TODAY.plusMonths(3).minusDays(1)
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
      given(reviewScheduleService.getActiveReviewScheduleForPrisoner(any())).willReturn(activeReviewSchedule)
      val updatedReviewSchedule = activeReviewSchedule.copy(scheduleStatus = ReviewScheduleStatus.COMPLETED)
      given(reviewSchedulePersistenceAdapter.updateReviewSchedule(any())).willReturn(updatedReviewSchedule)

      val completedReview = aValidCompletedReview()
      given(reviewPersistenceAdapter.createCompletedReview(any(), any())).willReturn(completedReview)

      given(
        reviewScheduleDateCalculationService.determineReviewScheduleCalculationRule(
          any(),
          any(),
          anyOrNull(),
          any(),
          any(),
        ),
      )
        .willThrow(ReviewScheduleNoReleaseDateForSentenceTypeException(PRISON_NUMBER, SentenceType.SENTENCED))

      // When
      val exception = catchThrowableOfType(ReviewScheduleNoReleaseDateForSentenceTypeException::class.java) {
        service.createReview(createCompletedReviewDto)
      }

      // Then
      assertThat(exception)
        .isInstanceOf(ReviewScheduleNoReleaseDateForSentenceTypeException::class.java)
        .extracting("prisonNumber", "sentenceType")
        .containsExactly(PRISON_NUMBER, SentenceType.SENTENCED)
      verify(reviewScheduleService).getActiveReviewScheduleForPrisoner(PRISON_NUMBER)
      verify(reviewScheduleDateCalculationService).determineReviewScheduleCalculationRule(
        PRISON_NUMBER,
        SentenceType.SENTENCED,
        releaseDate,
        false,
        false,
      )
      verifyNoInteractions(reviewEventService)
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

      given(reviewScheduleService.getActiveReviewScheduleForPrisoner(any()))
        .willThrow(ReviewScheduleNotFoundException(PRISON_NUMBER))

      // When
      val exception = catchThrowableOfType(ReviewScheduleNotFoundException::class.java) {
        service.createReview(createCompletedReviewDto)
      }

      // Then
      assertThat(exception.prisonNumber).isEqualTo(PRISON_NUMBER)
      verify(reviewScheduleService).getActiveReviewScheduleForPrisoner(PRISON_NUMBER)
      verifyNoMoreInteractions(reviewSchedulePersistenceAdapter)
      verifyNoInteractions(reviewPersistenceAdapter)
      verifyNoInteractions(reviewEventService)
    }
  }

  companion object {
    private val PRISON_NUMBER = randomValidPrisonNumber()
    private val TODAY = LocalDate.now()

    @JvmStatic
    fun notPrisonersLastReview_testCases(): Stream<Arguments> = Stream.of(
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
        "prisoner is has been recalled - next review 2 to 3 months",
        null,
        SentenceType.RECALL,
        false,
        true,
        ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
        ReviewScheduleWindow(TODAY.plusMonths(2), TODAY.plusMonths(3)),
      ),
      Arguments.of(
        "prisoner is sentenced with 6 months to serve - next review 2 to 3 months",
        TODAY.plusMonths(6),
        SentenceType.SENTENCED,
        false,
        false,
        ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE,
        ReviewScheduleWindow(TODAY.plusMonths(2), TODAY.plusMonths(3)),
      ),
      Arguments.of(
        "prisoner is sentenced with release date in the past - next review 2 to 3 months",
        TODAY.minusMonths(1),
        SentenceType.SENTENCED,
        false,
        false,
        ReviewScheduleCalculationRule.RELEASE_DATE_IN_PAST,
        ReviewScheduleWindow(TODAY.plusMonths(2), TODAY.plusMonths(3)),
      ),
    )
  }
}
