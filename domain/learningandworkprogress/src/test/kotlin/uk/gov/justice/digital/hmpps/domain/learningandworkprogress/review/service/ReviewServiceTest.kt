package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidCompletedReview
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidReviewScheduleHistory
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.MINUTES
import java.util.UUID

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

  companion object {
    private const val PRISON_NUMBER = "A1234AB"
    private val TODAY = LocalDate.now()
  }

  @Nested
  inner class GetActiveReviewScheduleForPrisoner {
    @Test
    fun `should get active review schedule for prisoner`() {
      // Given
      val expected = aValidReviewSchedule()
      given(reviewSchedulePersistenceAdapter.getActiveReviewSchedule(any())).willReturn(expected)

      // When
      val actual = service.getActiveReviewScheduleForPrisoner(PRISON_NUMBER)

      // Then
      assertThat(actual).isEqualTo(expected)
      verify(reviewSchedulePersistenceAdapter).getActiveReviewSchedule(PRISON_NUMBER)
    }

    @Test
    fun `should fail to get review schedule for prisoner given review schedule does not exist`() {
      // Given
      given(reviewSchedulePersistenceAdapter.getActiveReviewSchedule(any())).willReturn(null)

      // When
      val exception = catchThrowableOfType(ReviewScheduleNotFoundException::class.java) {
        service.getActiveReviewScheduleForPrisoner(PRISON_NUMBER)
      }

      // Then
      assertThat(exception).hasMessage("Review Schedule not found for prisoner [$PRISON_NUMBER]")
      assertThat(exception.prisonNumber).isEqualTo(PRISON_NUMBER)
      verify(reviewSchedulePersistenceAdapter).getActiveReviewSchedule(PRISON_NUMBER)
    }
  }

  @Nested
  inner class GetLatestReviewScheduleForPrisoner {
    @Test
    fun `should get latest review schedule for prisoner`() {
      // Given
      val expected = aValidReviewSchedule()
      given(reviewSchedulePersistenceAdapter.getLatestReviewSchedule(any())).willReturn(expected)

      // When
      val actual = service.getLatestReviewScheduleForPrisoner(PRISON_NUMBER)

      // Then
      assertThat(actual).isEqualTo(expected)
      verify(reviewSchedulePersistenceAdapter).getLatestReviewSchedule(PRISON_NUMBER)
    }

    @Test
    fun `should fail to get review schedule for prisoner given review schedule does not exist`() {
      // Given
      given(reviewSchedulePersistenceAdapter.getLatestReviewSchedule(any())).willReturn(null)

      // When
      val exception = catchThrowableOfType(ReviewScheduleNotFoundException::class.java) {
        service.getLatestReviewScheduleForPrisoner(PRISON_NUMBER)
      }

      // Then
      assertThat(exception).hasMessage("Review Schedule not found for prisoner [$PRISON_NUMBER]")
      assertThat(exception.prisonNumber).isEqualTo(PRISON_NUMBER)
      verify(reviewSchedulePersistenceAdapter).getLatestReviewSchedule(PRISON_NUMBER)
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
  inner class GetReviewSchedulesForPrisoner {
    @Test
    fun `should get review schedules for prisoner, sorted by last updated and version`() {
      // Given
      val now = Instant.now()
      val reviewSchedule1Reference = UUID.randomUUID()
      val reviewSchedule2Reference = UUID.randomUUID()
      val reviewSchedule3Reference = UUID.randomUUID()

      given(reviewSchedulePersistenceAdapter.getReviewScheduleHistory(any())).willReturn(
        listOf(
          aValidReviewScheduleHistory(
            reference = reviewSchedule3Reference,
            version = 2,
            lastUpdatedAt = now.minus(1, MINUTES),
          ),
          aValidReviewScheduleHistory(
            reference = reviewSchedule3Reference,
            version = 1,
            lastUpdatedAt = now.minus(10, MINUTES),
          ),
          aValidReviewScheduleHistory(
            reference = reviewSchedule1Reference,
            version = 2,
            lastUpdatedAt = now.minus(365, DAYS),
          ),
          aValidReviewScheduleHistory(
            reference = reviewSchedule2Reference,
            version = 1,
            lastUpdatedAt = now.minus(5, DAYS),
          ),
          aValidReviewScheduleHistory(
            reference = reviewSchedule1Reference,
            version = 1,
            lastUpdatedAt = now.minus(400, DAYS),
          ),
          aValidReviewScheduleHistory(
            reference = reviewSchedule2Reference,
            version = 2,
            lastUpdatedAt = now.minus(4, DAYS),
          ),
        ),
      )

      val expected = listOf(
        // Review schedule 3 first as it's updated dates are the most recent
        "Reference: $reviewSchedule3Reference; Version: 2",
        "Reference: $reviewSchedule3Reference; Version: 1",
        // Review schedule 2 next
        "Reference: $reviewSchedule2Reference; Version: 2",
        "Reference: $reviewSchedule2Reference; Version: 1",
        // Review schedule 1 last as it's updated dates are the earliest
        "Reference: $reviewSchedule1Reference; Version: 2",
        "Reference: $reviewSchedule1Reference; Version: 1",
      )

      // When
      val actual = service.getReviewSchedulesForPrisoner(PRISON_NUMBER)

      // Then
      assertThat(actual.map { "Reference: ${it.reference}; Version: ${it.version}" }).isEqualTo(expected)
      verify(reviewSchedulePersistenceAdapter).getReviewScheduleHistory(PRISON_NUMBER)
    }

    @Test
    fun `should get review schedules given prisoner has no previous review schedules`() {
      // Given
      given(reviewSchedulePersistenceAdapter.getReviewScheduleHistory(any())).willReturn(emptyList())

      // When
      val actual = service.getReviewSchedulesForPrisoner(PRISON_NUMBER)

      // Then
      assertThat(actual).isEmpty()
      verify(reviewSchedulePersistenceAdapter).getReviewScheduleHistory(PRISON_NUMBER)
    }
  }
}
