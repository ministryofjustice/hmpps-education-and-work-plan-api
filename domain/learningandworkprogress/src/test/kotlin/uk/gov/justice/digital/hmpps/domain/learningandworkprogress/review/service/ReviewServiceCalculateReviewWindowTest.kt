package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleWindow
import java.time.LocalDate

// TODO - delete this test when calculating the next review for a transfer or readmission have a service method - this logic will be tested by the tests for the service method
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

  companion object {
    private val TODAY = LocalDate.now()
  }
}
