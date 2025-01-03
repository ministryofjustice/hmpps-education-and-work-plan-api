package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import org.assertj.core.api.Assertions.assertThat
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
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.UpdateReviewScheduleStatusDto
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class ReviewScheduleServiceTest {
  @InjectMocks
  private lateinit var reviewScheduleService: ReviewScheduleService

  @Mock
  private lateinit var reviewSchedulePersistenceAdapter: ReviewSchedulePersistenceAdapter

  @Mock
  private lateinit var reviewScheduleEventService: ReviewScheduleEventService

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
        latestReviewDate = LocalDate.now().plusDays(2),
      )
      given(reviewSchedulePersistenceAdapter.getActiveReviewSchedule(any())).willReturn(activeReviewSchedule)

      val firstUpdatedReviewSchedule = activeReviewSchedule.copy(
        scheduleStatus = ReviewScheduleStatus.EXEMPT_PRISONER_TRANSFER,
        lastUpdatedAtPrison = newPrisonId,
      )

      val expectedNewReviewScheduleDeadlineDate = LocalDate.now().plusDays(5)

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
}
