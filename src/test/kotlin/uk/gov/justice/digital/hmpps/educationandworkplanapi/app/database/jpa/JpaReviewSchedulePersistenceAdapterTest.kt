package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidReviewSchedule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.aValidReviewScheduleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.review.ReviewScheduleEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewScheduleRepository

@ExtendWith(MockitoExtension::class)
class JpaReviewSchedulePersistenceAdapterTest {

  @InjectMocks
  private lateinit var persistenceAdapter: JpaReviewSchedulePersistenceAdapter

  @Mock
  private lateinit var reviewScheduleRepository: ReviewScheduleRepository

  @Mock
  private lateinit var reviewScheduleEntityMapper: ReviewScheduleEntityMapper

  @Nested
  inner class GetReviewSchedule {
    @Test
    fun `should get review schedule given review schedule exists for prisoner`() {
      // Given
      val prisonNumber = aValidPrisonNumber()

      val reviewScheduleEntity = aValidReviewScheduleEntity()
      given(reviewScheduleRepository.findByPrisonNumber(any())).willReturn(reviewScheduleEntity)

      val reviewSchedule = aValidReviewSchedule()
      given(reviewScheduleEntityMapper.fromEntityToDomain(any())).willReturn(reviewSchedule)

      // When
      val actual = persistenceAdapter.getReviewSchedule(prisonNumber)

      // Then
      assertThat(actual).isEqualTo(reviewSchedule)
      verify(reviewScheduleRepository).findByPrisonNumber(prisonNumber)
      verify(reviewScheduleEntityMapper).fromEntityToDomain(reviewScheduleEntity)
    }

    @Test
    fun `should not get review schedule given review schedule does not exist for prisoner`() {
      // Given
      val prisonNumber = aValidPrisonNumber()

      given(reviewScheduleRepository.findByPrisonNumber(any())).willReturn(null)

      // When
      val actual = persistenceAdapter.getReviewSchedule(prisonNumber)

      // Then
      assertThat(actual).isNull()
      verify(reviewScheduleRepository).findByPrisonNumber(prisonNumber)
      verifyNoInteractions(reviewScheduleEntityMapper)
    }
  }
}
