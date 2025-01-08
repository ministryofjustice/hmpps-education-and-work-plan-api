package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.UpdatedReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidReviewScheduleHistory
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.UpdateReviewScheduleStatusDto
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.MINUTES
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ReviewScheduleServiceTest {
  @InjectMocks
  private lateinit var reviewScheduleService: ReviewScheduleService

  @Mock
  private lateinit var reviewSchedulePersistenceAdapter: ReviewSchedulePersistenceAdapter

  @Mock
  private lateinit var reviewScheduleEventService: ReviewScheduleEventService

  companion object {
    private val PRISON_NUMBER = randomValidPrisonNumber()
    private val TODAY = LocalDate.now()
    private val NOW = Instant.now()
  }

  @Nested
  inner class GetLatestReviewScheduleForPrisoner {
    @Test
    fun `should get latest review schedule for prisoner`() {
      // Given
      val expected = aValidReviewSchedule()
      given(reviewSchedulePersistenceAdapter.getLatestReviewSchedule(any())).willReturn(expected)

      // When
      val actual = reviewScheduleService.getLatestReviewScheduleForPrisoner(PRISON_NUMBER)

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
        reviewScheduleService.getLatestReviewScheduleForPrisoner(PRISON_NUMBER)
      }

      // Then
      assertThat(exception).hasMessage("Review Schedule not found for prisoner [$PRISON_NUMBER]")
      assertThat(exception.prisonNumber).isEqualTo(PRISON_NUMBER)
      verify(reviewSchedulePersistenceAdapter).getLatestReviewSchedule(PRISON_NUMBER)
    }
  }

  @Nested
  inner class ExemptActiveReviewScheduleStatusDueToPrisonerRelease {
    @Test
    fun `should exempt active Review Schedule status for prisoner`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val prisonId = "BXI"

      val activeReviewSchedule = aValidReviewSchedule(
        prisonNumber = prisonNumber,
        scheduleStatus = ReviewScheduleStatus.SCHEDULED,
      )
      given(reviewSchedulePersistenceAdapter.getActiveReviewSchedule(any())).willReturn(activeReviewSchedule)

      val updatedReviewSchedule = activeReviewSchedule.copy(
        scheduleStatus = ReviewScheduleStatus.EXEMPT_PRISONER_RELEASE,
      )
      given(reviewSchedulePersistenceAdapter.updateReviewScheduleStatus(any())).willReturn(updatedReviewSchedule)

      // When
      reviewScheduleService.exemptActiveReviewScheduleStatusDueToPrisonerRelease(prisonNumber, prisonId)

      // Then
      verify(reviewSchedulePersistenceAdapter).getActiveReviewSchedule(prisonNumber)

      val updateReviewScheduleStatusDtoCaptor = argumentCaptor<UpdateReviewScheduleStatusDto>()
      verify(reviewSchedulePersistenceAdapter).updateReviewScheduleStatus(updateReviewScheduleStatusDtoCaptor.capture())
      val updateReviewScheduleStatusDto = updateReviewScheduleStatusDtoCaptor.firstValue
      assertThat(updateReviewScheduleStatusDto.reference).isEqualTo(activeReviewSchedule.reference)
      assertThat(updateReviewScheduleStatusDto.prisonNumber).isEqualTo(prisonNumber)
      assertThat(updateReviewScheduleStatusDto.prisonId).isEqualTo(prisonId)
      assertThat(updateReviewScheduleStatusDto.scheduleStatus).isEqualTo(ReviewScheduleStatus.EXEMPT_PRISONER_RELEASE)

      val updateReviewScheduleStatusCaptor = argumentCaptor<UpdatedReviewScheduleStatus>()
      verify(reviewScheduleEventService).reviewScheduleStatusUpdated(updateReviewScheduleStatusCaptor.capture())
      val updateReviewScheduleStatus = updateReviewScheduleStatusCaptor.firstValue
      assertThat(updateReviewScheduleStatus.reference).isEqualTo(updatedReviewSchedule.reference)
      assertThat(updateReviewScheduleStatus.prisonNumber).isEqualTo(prisonNumber)
      assertThat(updateReviewScheduleStatus.updatedAtPrison).isEqualTo(prisonId)
      assertThat(updateReviewScheduleStatus.oldStatus).isEqualTo(ReviewScheduleStatus.SCHEDULED)
      assertThat(updateReviewScheduleStatus.newStatus).isEqualTo(ReviewScheduleStatus.EXEMPT_PRISONER_RELEASE)
    }

    @Test
    fun `should not exempt Review Schedule status given prisoner does not have an active review schedule`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val prisonId = "BXI"

      given(reviewSchedulePersistenceAdapter.getActiveReviewSchedule(any())).willReturn(null)

      // When
      val exception = assertThrows(ReviewScheduleNotFoundException::class.java) {
        reviewScheduleService.exemptActiveReviewScheduleStatusDueToPrisonerRelease(prisonNumber, prisonId)
      }

      // Then
      assertThat(exception.prisonNumber).isEqualTo(prisonNumber)
      verify(reviewSchedulePersistenceAdapter).getActiveReviewSchedule(prisonNumber)
      verifyNoInteractions(reviewScheduleEventService)
    }
  }

  @Nested
  inner class ExemptActiveReviewScheduleStatusDueToPrisonerDeath {
    @Test
    fun `should exempt active Review Schedule status for prisoner`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val prisonId = "BXI"

      val activeReviewSchedule = aValidReviewSchedule(
        prisonNumber = prisonNumber,
        scheduleStatus = ReviewScheduleStatus.SCHEDULED,
      )
      given(reviewSchedulePersistenceAdapter.getActiveReviewSchedule(any())).willReturn(activeReviewSchedule)

      val updatedReviewSchedule = activeReviewSchedule.copy(
        scheduleStatus = ReviewScheduleStatus.EXEMPT_PRISONER_DEATH,
      )
      given(reviewSchedulePersistenceAdapter.updateReviewScheduleStatus(any())).willReturn(updatedReviewSchedule)

      // When
      reviewScheduleService.exemptActiveReviewScheduleStatusDueToPrisonerDeath(prisonNumber, prisonId)

      // Then
      verify(reviewSchedulePersistenceAdapter).getActiveReviewSchedule(prisonNumber)

      val updateReviewScheduleStatusDtoCaptor = argumentCaptor<UpdateReviewScheduleStatusDto>()
      verify(reviewSchedulePersistenceAdapter).updateReviewScheduleStatus(updateReviewScheduleStatusDtoCaptor.capture())
      val updateReviewScheduleStatusDto = updateReviewScheduleStatusDtoCaptor.firstValue
      assertThat(updateReviewScheduleStatusDto.reference).isEqualTo(activeReviewSchedule.reference)
      assertThat(updateReviewScheduleStatusDto.prisonNumber).isEqualTo(prisonNumber)
      assertThat(updateReviewScheduleStatusDto.prisonId).isEqualTo(prisonId)
      assertThat(updateReviewScheduleStatusDto.scheduleStatus).isEqualTo(ReviewScheduleStatus.EXEMPT_PRISONER_DEATH)

      val updateReviewScheduleStatusCaptor = argumentCaptor<UpdatedReviewScheduleStatus>()
      verify(reviewScheduleEventService).reviewScheduleStatusUpdated(updateReviewScheduleStatusCaptor.capture())
      val updateReviewScheduleStatus = updateReviewScheduleStatusCaptor.firstValue
      assertThat(updateReviewScheduleStatus.reference).isEqualTo(updatedReviewSchedule.reference)
      assertThat(updateReviewScheduleStatus.prisonNumber).isEqualTo(prisonNumber)
      assertThat(updateReviewScheduleStatus.updatedAtPrison).isEqualTo(prisonId)
      assertThat(updateReviewScheduleStatus.oldStatus).isEqualTo(ReviewScheduleStatus.SCHEDULED)
      assertThat(updateReviewScheduleStatus.newStatus).isEqualTo(ReviewScheduleStatus.EXEMPT_PRISONER_DEATH)
    }

    @Test
    fun `should not exempt Review Schedule status given prisoner does not have an active review schedule`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val prisonId = "BXI"

      given(reviewSchedulePersistenceAdapter.getActiveReviewSchedule(any())).willReturn(null)

      // When
      val exception = assertThrows(ReviewScheduleNotFoundException::class.java) {
        reviewScheduleService.exemptActiveReviewScheduleStatusDueToPrisonerDeath(prisonNumber, prisonId)
      }

      // Then
      assertThat(exception.prisonNumber).isEqualTo(prisonNumber)
      verify(reviewSchedulePersistenceAdapter).getActiveReviewSchedule(prisonNumber)
      verifyNoInteractions(reviewScheduleEventService)
    }
  }

  @Nested
  inner class ExemptAndReScheduleActiveReviewScheduleStatusDueToPrisonerTransfer {
    @Test
    fun `should exempt active Review Schedule status for prisoner`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val originalPrisonId = "BXI"
      val newPrisonId = "MDI"

      val activeReviewSchedule = aValidReviewSchedule(
        prisonNumber = prisonNumber,
        scheduleStatus = ReviewScheduleStatus.SCHEDULED,
        latestReviewDate = TODAY.plusDays(2),
      )
      given(reviewSchedulePersistenceAdapter.getActiveReviewSchedule(any())).willReturn(activeReviewSchedule)

      val firstUpdatedReviewSchedule = activeReviewSchedule.copy(
        scheduleStatus = ReviewScheduleStatus.EXEMPT_PRISONER_TRANSFER,
        lastUpdatedAtPrison = newPrisonId,
      )

      val expectedNewReviewScheduleDeadlineDate = TODAY.plusDays(5)

      val secondUpdatedReviewSchedule = firstUpdatedReviewSchedule.copy(
        scheduleStatus = ReviewScheduleStatus.SCHEDULED,
        reviewScheduleWindow = firstUpdatedReviewSchedule.reviewScheduleWindow.copy(dateTo = expectedNewReviewScheduleDeadlineDate),
      )
      given(reviewSchedulePersistenceAdapter.updateReviewScheduleStatus(any())).willReturn(firstUpdatedReviewSchedule, secondUpdatedReviewSchedule)

      // When
      reviewScheduleService.exemptAndReScheduleActiveReviewScheduleStatusDueToPrisonerTransfer(prisonNumber, newPrisonId)

      // Then
      verify(reviewSchedulePersistenceAdapter).getActiveReviewSchedule(prisonNumber)

      val updateReviewScheduleStatusDtoCaptor = argumentCaptor<UpdateReviewScheduleStatusDto>()
      verify(reviewSchedulePersistenceAdapter, times(2)).updateReviewScheduleStatus(updateReviewScheduleStatusDtoCaptor.capture())
      // First call to the persistence adapter should be to mark the Review Schedule as Exempt due to Prisoner Transfer
      var updateReviewScheduleStatusDto = updateReviewScheduleStatusDtoCaptor.firstValue
      assertThat(updateReviewScheduleStatusDto.reference).isEqualTo(activeReviewSchedule.reference)
      assertThat(updateReviewScheduleStatusDto.prisonNumber).isEqualTo(prisonNumber)
      assertThat(updateReviewScheduleStatusDto.prisonId).isEqualTo(originalPrisonId)
      assertThat(updateReviewScheduleStatusDto.scheduleStatus).isEqualTo(ReviewScheduleStatus.EXEMPT_PRISONER_TRANSFER)
      // Second call to the persistence adapter should be to mark the Review Schedule as Scheduled
      updateReviewScheduleStatusDto = updateReviewScheduleStatusDtoCaptor.secondValue
      assertThat(updateReviewScheduleStatusDto.reference).isEqualTo(activeReviewSchedule.reference)
      assertThat(updateReviewScheduleStatusDto.prisonNumber).isEqualTo(prisonNumber)
      assertThat(updateReviewScheduleStatusDto.prisonId).isEqualTo(newPrisonId)
      assertThat(updateReviewScheduleStatusDto.scheduleStatus).isEqualTo(ReviewScheduleStatus.SCHEDULED)
      assertThat(updateReviewScheduleStatusDto.latestReviewDate).isEqualTo(expectedNewReviewScheduleDeadlineDate)

      val updateReviewScheduleStatusCaptor = argumentCaptor<UpdatedReviewScheduleStatus>()
      verify(reviewScheduleEventService, times(2)).reviewScheduleStatusUpdated(updateReviewScheduleStatusCaptor.capture())
      // First call to the event service should be because of the Exemption of the Review Schedule due to Prisoner Transfer
      var updateReviewScheduleStatus = updateReviewScheduleStatusCaptor.firstValue
      assertThat(updateReviewScheduleStatus.reference).isEqualTo(firstUpdatedReviewSchedule.reference)
      assertThat(updateReviewScheduleStatus.prisonNumber).isEqualTo(prisonNumber)
      assertThat(updateReviewScheduleStatus.updatedAtPrison).isEqualTo(newPrisonId)
      assertThat(updateReviewScheduleStatus.oldStatus).isEqualTo(ReviewScheduleStatus.SCHEDULED)
      assertThat(updateReviewScheduleStatus.newStatus).isEqualTo(ReviewScheduleStatus.EXEMPT_PRISONER_TRANSFER)
      // Second call to the event service should be because of the re-scheduling of the Review Schedule
      updateReviewScheduleStatus = updateReviewScheduleStatusCaptor.secondValue
      assertThat(updateReviewScheduleStatus.reference).isEqualTo(firstUpdatedReviewSchedule.reference)
      assertThat(updateReviewScheduleStatus.prisonNumber).isEqualTo(prisonNumber)
      assertThat(updateReviewScheduleStatus.updatedAtPrison).isEqualTo(newPrisonId)
      assertThat(updateReviewScheduleStatus.oldStatus).isEqualTo(ReviewScheduleStatus.EXEMPT_PRISONER_TRANSFER)
      assertThat(updateReviewScheduleStatus.newStatus).isEqualTo(ReviewScheduleStatus.SCHEDULED)
      assertThat(updateReviewScheduleStatus.newReviewDate).isEqualTo(expectedNewReviewScheduleDeadlineDate)
    }

    @Test
    fun `should not exempt and re-schedule Review Schedule status given prisoner does not have an active review schedule`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val originalPrisonId = "BXI"
      val newPrisonId = "MDI"

      given(reviewSchedulePersistenceAdapter.getActiveReviewSchedule(any())).willReturn(null)

      // When
      val exception = assertThrows(ReviewScheduleNotFoundException::class.java) {
        reviewScheduleService.exemptAndReScheduleActiveReviewScheduleStatusDueToPrisonerTransfer(prisonNumber, newPrisonId)
      }

      // Then
      assertThat(exception.prisonNumber).isEqualTo(prisonNumber)
      verify(reviewSchedulePersistenceAdapter).getActiveReviewSchedule(prisonNumber)
      verifyNoInteractions(reviewScheduleEventService)
    }
  }

  @Nested
  inner class GetReviewSchedulesForPrisoner {
    @Test
    fun `should get review schedules for prisoner, sorted by last updated and version`() {
      // Given
      val reviewSchedule1Reference = UUID.randomUUID()
      val reviewSchedule2Reference = UUID.randomUUID()
      val reviewSchedule3Reference = UUID.randomUUID()

      given(reviewSchedulePersistenceAdapter.getReviewScheduleHistory(any())).willReturn(
        listOf(
          aValidReviewScheduleHistory(
            reference = reviewSchedule3Reference,
            version = 2,
            lastUpdatedAt = NOW.minus(1, MINUTES),
          ),
          aValidReviewScheduleHistory(
            reference = reviewSchedule3Reference,
            version = 1,
            lastUpdatedAt = NOW.minus(10, MINUTES),
          ),
          aValidReviewScheduleHistory(
            reference = reviewSchedule1Reference,
            version = 2,
            lastUpdatedAt = NOW.minus(365, DAYS),
          ),
          aValidReviewScheduleHistory(
            reference = reviewSchedule2Reference,
            version = 1,
            lastUpdatedAt = NOW.minus(5, DAYS),
          ),
          aValidReviewScheduleHistory(
            reference = reviewSchedule1Reference,
            version = 1,
            lastUpdatedAt = NOW.minus(400, DAYS),
          ),
          aValidReviewScheduleHistory(
            reference = reviewSchedule2Reference,
            version = 2,
            lastUpdatedAt = NOW.minus(4, DAYS),
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
      val actual = reviewScheduleService.getReviewSchedulesForPrisoner(PRISON_NUMBER)

      // Then
      assertThat(actual.map { "Reference: ${it.reference}; Version: ${it.version}" }).isEqualTo(expected)
      verify(reviewSchedulePersistenceAdapter).getReviewScheduleHistory(PRISON_NUMBER)
    }

    @Test
    fun `should get review schedules given prisoner has no previous review schedules`() {
      // Given
      given(reviewSchedulePersistenceAdapter.getReviewScheduleHistory(any())).willReturn(emptyList())

      // When
      val actual = reviewScheduleService.getReviewSchedulesForPrisoner(PRISON_NUMBER)

      // Then
      assertThat(actual).isEmpty()
      verify(reviewSchedulePersistenceAdapter).getReviewScheduleHistory(PRISON_NUMBER)
    }
  }

  @Nested
  inner class GetActiveReviewScheduleForPrisoner {
    @Test
    fun `should get active review schedule for prisoner`() {
      // Given
      val expected = aValidReviewSchedule()
      given(reviewSchedulePersistenceAdapter.getActiveReviewSchedule(any())).willReturn(expected)

      // When
      val actual = reviewScheduleService.getActiveReviewScheduleForPrisoner(PRISON_NUMBER)

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
        reviewScheduleService.getActiveReviewScheduleForPrisoner(PRISON_NUMBER)
      }

      // Then
      assertThat(exception).hasMessage("Review Schedule not found for prisoner [$PRISON_NUMBER]")
      assertThat(exception.prisonNumber).isEqualTo(PRISON_NUMBER)
      verify(reviewSchedulePersistenceAdapter).getActiveReviewSchedule(PRISON_NUMBER)
    }
  }
}
