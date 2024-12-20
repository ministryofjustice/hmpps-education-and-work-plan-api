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
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.UpdatedReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.UpdateReviewScheduleStatusDto

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
}
