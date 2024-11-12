package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleWindow
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class ReviewServiceCalculateReviewWindowTest {
  @InjectMocks
  private lateinit var service: ReviewService

  @Mock
  private lateinit var reviewPersistenceAdapter: ReviewPersistenceAdapter

  @Mock
  private lateinit var reviewSchedulePersistenceAdapter: ReviewSchedulePersistenceAdapter

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

  companion object {
    private const val PRISON_NUMBER = "A1234AB"
    private val TODAY = LocalDate.now()
  }
}
