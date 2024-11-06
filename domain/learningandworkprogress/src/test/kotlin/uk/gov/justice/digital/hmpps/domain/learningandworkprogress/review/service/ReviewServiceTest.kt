package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidCompletedReview
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidReviewSchedule
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class ReviewServiceTest {
  @InjectMocks
  private lateinit var service: ReviewService

  @Mock
  private lateinit var reviewPersistenceAdapter: ReviewPersistenceAdapter

  @Mock
  private lateinit var reviewSchedulePersistenceAdapter: ReviewSchedulePersistenceAdapter

  companion object {
    private const val PRISON_NUMBER = "A1234AB"
    private val TODAY = LocalDate.now()
  }

  @Nested
  inner class GetCompletedReviewScheduleForPrisoner {
    @Test
    fun `should get review schedule for prisoner`() {
      // Given
      val expected = aValidReviewSchedule()
      given(reviewSchedulePersistenceAdapter.getReviewSchedule(any())).willReturn(expected)

      // When
      val actual = service.getReviewScheduleForPrisoner(PRISON_NUMBER)

      // Then
      assertThat(actual).isEqualTo(expected)
      verify(reviewSchedulePersistenceAdapter).getReviewSchedule(PRISON_NUMBER)
    }

    @Test
    fun `should fail to get review schedule for prisoner given review schedule does not exist`() {
      // Given
      given(reviewSchedulePersistenceAdapter.getReviewSchedule(any())).willReturn(null)

      // When
      val exception = catchThrowableOfType(ReviewScheduleNotFoundException::class.java) {
        service.getReviewScheduleForPrisoner(PRISON_NUMBER)
      }

      // Then
      assertThat(exception).hasMessage("Review Schedule not found for prisoner [$PRISON_NUMBER]")
      assertThat(exception.prisonNumber).isEqualTo(PRISON_NUMBER)
      verify(reviewSchedulePersistenceAdapter).getReviewSchedule(PRISON_NUMBER)
    }
  }

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
  inner class CalculateReviewWindow {
    @ParameterizedTest
    @CsvSource(
      value = [
        "PRISONER_TRANSFER",
        "PRISONER_READMISSION",
      ],
    )
    fun `should calculate a 'today to today + 10 days' review window`(calculationRule: ReviewScheduleCalculationRule) {
      // Given
      val expected = ReviewScheduleWindow(TODAY, TODAY.plusDays(10))

      // When
      val actual = service.calculateReviewWindow(calculationRule)

      // Then
      assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `should calculate a '1 to 3 months' review window`() {
      // Given
      val calculationRule = ReviewScheduleCalculationRule.LESS_THAN_6_MONTHS_TO_SERVE

      val expected = ReviewScheduleWindow(TODAY.plusMonths(1), TODAY.plusMonths(3))

      // When
      val actual = service.calculateReviewWindow(calculationRule)

      // Then
      assertThat(actual).isEqualTo(expected)
    }

    @ParameterizedTest
    @CsvSource(
      value = [
        "BETWEEN_6_AND_12_MONTHS_TO_SERVE",
        "PRISONER_ON_REMAND",
        "PRISONER_UN_SENTENCED",
      ],
    )
    fun `should calculate a '2 to 3 months' review window`(calculationRule: ReviewScheduleCalculationRule) {
      // Given
      val expected = ReviewScheduleWindow(TODAY.plusMonths(2), TODAY.plusMonths(3))

      // When
      val actual = service.calculateReviewWindow(calculationRule)

      // Then
      assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `should calculate a '4 to 6 months' review window`() {
      // Given
      val calculationRule = ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE

      val expected = ReviewScheduleWindow(TODAY.plusMonths(4), TODAY.plusMonths(6))

      // When
      val actual = service.calculateReviewWindow(calculationRule)

      // Then
      assertThat(actual).isEqualTo(expected)
    }

    @ParameterizedTest
    @CsvSource(
      value = [
        "MORE_THAN_60_MONTHS_TO_SERVE",
        "INDETERMINATE_SENTENCE",
      ],
    )
    fun `should calculate a '10 to 12 months' review window`(calculationRule: ReviewScheduleCalculationRule) {
      // Given
      val expected = ReviewScheduleWindow(TODAY.plusMonths(10), TODAY.plusMonths(12))

      // When
      val actual = service.calculateReviewWindow(calculationRule)

      // Then
      assertThat(actual).isEqualTo(expected)
    }
  }
}
