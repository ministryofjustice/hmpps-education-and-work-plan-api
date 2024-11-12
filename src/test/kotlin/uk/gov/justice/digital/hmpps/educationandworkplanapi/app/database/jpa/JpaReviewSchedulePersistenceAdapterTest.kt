package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
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
import org.mockito.kotlin.verifyNoMoreInteractions
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.aValidReference
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.aValidCreateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.aValidUpdateReviewScheduleDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleEntity
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

  @Nested
  inner class CreateReviewSchedule {
    @Test
    fun `should create review schedule given prisoner does not already have one`() {
      // Given
      val prisonNumber = aValidPrisonNumber()

      val createReviewScheduleDto = aValidCreateReviewScheduleDto(
        prisonNumber = prisonNumber,
      )

      given(reviewScheduleRepository.findByPrisonNumber(any())).willReturn(null)

      val reviewScheduleEntity = aValidReviewScheduleEntity(
        prisonNumber = prisonNumber,
      )
      given(reviewScheduleEntityMapper.fromDomainToEntity(any())).willReturn(reviewScheduleEntity)
      given(reviewScheduleRepository.saveAndFlush(any<ReviewScheduleEntity>())).willReturn(reviewScheduleEntity)

      val expectedReviewSchedule = aValidReviewSchedule()
      given(reviewScheduleEntityMapper.fromEntityToDomain(any())).willReturn(expectedReviewSchedule)

      // When
      val actual = persistenceAdapter.createReviewSchedule(createReviewScheduleDto)

      // Then
      assertThat(actual).isEqualTo(expectedReviewSchedule)
      verify(reviewScheduleRepository).findByPrisonNumber(prisonNumber)
      verify(reviewScheduleEntityMapper).fromDomainToEntity(createReviewScheduleDto)
      verify(reviewScheduleRepository).saveAndFlush(reviewScheduleEntity)
      verify(reviewScheduleEntityMapper).fromEntityToDomain(reviewScheduleEntity)
    }
  }

  @Test
  fun `should not create review schedule given prisoner already has one`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    val createReviewScheduleDto = aValidCreateReviewScheduleDto(
      prisonNumber = prisonNumber,
    )

    val reviewScheduleEntity = aValidReviewScheduleEntity(
      prisonNumber = prisonNumber,
    )
    given(reviewScheduleRepository.findByPrisonNumber(any())).willReturn(reviewScheduleEntity)

    // When
    val exception = assertThrows(ReviewScheduleAlreadyExistsException::class.java) {
      persistenceAdapter.createReviewSchedule(createReviewScheduleDto)
    }

    // Then
    assertThat(exception.prisonNumber).isEqualTo(prisonNumber)
    verify(reviewScheduleRepository).findByPrisonNumber(prisonNumber)
    verifyNoMoreInteractions(reviewScheduleRepository)
    verifyNoInteractions(reviewScheduleEntityMapper)
  }

  @Nested
  inner class UpdateReviewSchedule {
    @Test
    fun `should update review schedule`() {
      // Given
      val reference = aValidReference()

      val reviewScheduleEntity = aValidReviewScheduleEntity(
        reference = reference,
      )
      given(reviewScheduleRepository.findByReference(any())).willReturn(reviewScheduleEntity)
      given(reviewScheduleRepository.saveAndFlush(any<ReviewScheduleEntity>())).willReturn(reviewScheduleEntity)

      val expectedReviewSchedule = aValidReviewSchedule()
      given(reviewScheduleEntityMapper.fromEntityToDomain(any())).willReturn(expectedReviewSchedule)

      val updateReviewScheduleDto = aValidUpdateReviewScheduleDto(
        reference = reference,
      )

      // When
      val actual = persistenceAdapter.updateReviewSchedule(updateReviewScheduleDto)

      // Then
      assertThat(actual).isEqualTo(expectedReviewSchedule)
      verify(reviewScheduleRepository).findByReference(reference)
      verify(reviewScheduleEntityMapper).updateExistingEntityFromDto(reviewScheduleEntity, updateReviewScheduleDto)
      verify(reviewScheduleRepository).saveAndFlush(reviewScheduleEntity)
      verify(reviewScheduleEntityMapper).fromEntityToDomain(reviewScheduleEntity)
    }

    @Test
    fun `should not update review schedule given review does not exist by reference`() {
      // Given
      val reference = aValidReference()

      given(reviewScheduleRepository.findByReference(any())).willReturn(null)

      val updateReviewScheduleDto = aValidUpdateReviewScheduleDto(
        reference = reference,
      )

      // When
      val actual = persistenceAdapter.updateReviewSchedule(updateReviewScheduleDto)

      // Then
      assertThat(actual).isNull()
      verify(reviewScheduleRepository).findByReference(reference)
      verifyNoMoreInteractions(reviewScheduleRepository)
      verifyNoInteractions(reviewScheduleEntityMapper)
    }
  }
}
