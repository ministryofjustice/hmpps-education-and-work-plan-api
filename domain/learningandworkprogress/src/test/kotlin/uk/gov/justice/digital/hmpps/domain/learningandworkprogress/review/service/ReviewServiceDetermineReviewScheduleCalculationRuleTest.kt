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
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType
import java.time.LocalDate
import java.util.stream.Stream

// TODO - delete this test when calculating the next review for a transfer or readmission have a service method - this logic will be tested by the tests for the service method
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
      )
  }
}
