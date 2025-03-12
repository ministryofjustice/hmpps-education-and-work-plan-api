package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import jakarta.persistence.NonUniqueResultException
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
import uk.gov.justice.digital.hmpps.domain.aValidReference
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ActiveReviewScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.aValidCreateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.aValidUpdateReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus.Companion.STATUSES_FOR_ACTIVE_REVIEWS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.aValidReviewScheduleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.review.ReviewScheduleEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewScheduleHistoryRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewScheduleRepository
import java.time.Instant
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus as ReviewScheduleStatusDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus as ReviewScheduleStatusEntity

@ExtendWith(MockitoExtension::class)
class JpaReviewSchedulePersistenceAdapterTest {

  @InjectMocks
  private lateinit var persistenceAdapter: JpaReviewSchedulePersistenceAdapter

  @Mock
  private lateinit var reviewScheduleRepository: ReviewScheduleRepository

  @Mock
  private lateinit var reviewScheduleHistoryRepository: ReviewScheduleHistoryRepository

  @Mock
  private lateinit var reviewScheduleEntityMapper: ReviewScheduleEntityMapper

  @Nested
  inner class GetActiveReviewSchedule {
    @Test
    fun `should get active review schedule given review schedules exists for prisoner`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()

      val activeReviewScheduleEntity = aValidReviewScheduleEntity(
        scheduleStatus = ReviewScheduleStatusEntity.SCHEDULED,
      )
      given(reviewScheduleRepository.findByPrisonNumberAndScheduleStatusIn(any(), any())).willReturn(activeReviewScheduleEntity)

      val activeReviewSchedule = aValidReviewSchedule(
        scheduleStatus = ReviewScheduleStatusDomain.SCHEDULED,
      )
      given(reviewScheduleEntityMapper.fromEntityToDomain(any())).willReturn(activeReviewSchedule)

      // When
      val actual = persistenceAdapter.getActiveReviewSchedule(prisonNumber)

      // Then
      assertThat(actual).isEqualTo(activeReviewSchedule)
      verify(reviewScheduleRepository).findByPrisonNumberAndScheduleStatusIn(prisonNumber, STATUSES_FOR_ACTIVE_REVIEWS)
      verify(reviewScheduleEntityMapper).fromEntityToDomain(activeReviewScheduleEntity)
    }

    @Test
    fun `should not get active review schedule given review schedule does not exist for prisoner`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()

      given(reviewScheduleRepository.findByPrisonNumberAndScheduleStatusIn(any(), any())).willReturn(null)

      // When
      val actual = persistenceAdapter.getActiveReviewSchedule(prisonNumber)

      // Then
      assertThat(actual).isNull()
      verify(reviewScheduleRepository).findByPrisonNumberAndScheduleStatusIn(prisonNumber, STATUSES_FOR_ACTIVE_REVIEWS)
      verifyNoInteractions(reviewScheduleEntityMapper)
    }

    @Test
    fun `should not get active review schedule given prisoner has multiple active review schedule records`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()

      given(reviewScheduleRepository.findByPrisonNumberAndScheduleStatusIn(any(), any())).willThrow(NonUniqueResultException())

      // When
      val exception = assertThrows(IllegalStateException::class.java) {
        persistenceAdapter.getActiveReviewSchedule(prisonNumber)
      }

      // Then
      assertThat(exception).hasMessage("A prisoner cannot have more than one active ReviewSchedule. Please investigate the ReviewSchedule data for prisoner $prisonNumber")
      verify(reviewScheduleRepository).findByPrisonNumberAndScheduleStatusIn(prisonNumber, STATUSES_FOR_ACTIVE_REVIEWS)
      verifyNoInteractions(reviewScheduleEntityMapper)
    }
  }

  @Nested
  inner class GetLatestReviewSchedule {
    @Test
    fun `should get latest review schedule given review schedules exists for prisoner`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()

      val latestReviewScheduleEntity = aValidReviewScheduleEntity(
        scheduleStatus = ReviewScheduleStatusEntity.SCHEDULED,
        updatedAt = Instant.now(),
      )
      given(reviewScheduleRepository.findFirstByPrisonNumberOrderByUpdatedAtDesc(any())).willReturn(latestReviewScheduleEntity)

      val latestReviewSchedule = aValidReviewSchedule()
      given(reviewScheduleEntityMapper.fromEntityToDomain(any())).willReturn(latestReviewSchedule)

      // When
      val actual = persistenceAdapter.getLatestReviewSchedule(prisonNumber)

      // Then
      assertThat(actual).isEqualTo(latestReviewSchedule)
      verify(reviewScheduleRepository).findFirstByPrisonNumberOrderByUpdatedAtDesc(prisonNumber)
      verify(reviewScheduleEntityMapper).fromEntityToDomain(latestReviewScheduleEntity)
    }

    @Test
    fun `should not get latest review schedule given review schedule does not exist for prisoner`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()

      given(reviewScheduleRepository.findFirstByPrisonNumberOrderByUpdatedAtDesc(any())).willReturn(null)

      // When
      val actual = persistenceAdapter.getLatestReviewSchedule(prisonNumber)

      // Then
      assertThat(actual).isNull()
      verify(reviewScheduleRepository).findFirstByPrisonNumberOrderByUpdatedAtDesc(prisonNumber)
      verifyNoInteractions(reviewScheduleEntityMapper)
    }
  }

  @Nested
  inner class CreateReviewSchedule {
    @Test
    fun `should create review schedule given prisoner does not already have one`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()

      val createReviewScheduleDto = aValidCreateReviewScheduleDto(
        prisonNumber = prisonNumber,
      )

      given(reviewScheduleRepository.findByPrisonNumberAndScheduleStatusIn(any(), any())).willReturn(null)

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
      verify(reviewScheduleHistoryRepository).findMaxVersionByReviewScheduleReference(reviewScheduleEntity.reference)
      verify(reviewScheduleRepository).findByPrisonNumberAndScheduleStatusIn(prisonNumber, STATUSES_FOR_ACTIVE_REVIEWS)
      verify(reviewScheduleEntityMapper).fromDomainToEntity(createReviewScheduleDto)
      verify(reviewScheduleRepository).saveAndFlush(reviewScheduleEntity)
      verify(reviewScheduleEntityMapper).fromEntityToDomain(reviewScheduleEntity)
      verify(reviewScheduleHistoryRepository).save(any())
    }
  }

  @Test
  fun `should not create review schedule given prisoner already has an active Review Schedule`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()

    val createReviewScheduleDto = aValidCreateReviewScheduleDto(
      prisonNumber = prisonNumber,
    )

    val reviewScheduleEntity = aValidReviewScheduleEntity(
      prisonNumber = prisonNumber,
      scheduleStatus = ReviewScheduleStatusEntity.SCHEDULED,
    )
    given(reviewScheduleRepository.findByPrisonNumberAndScheduleStatusIn(any(), any())).willReturn(reviewScheduleEntity)

    val reviewSchedule = aValidReviewSchedule()
    given(reviewScheduleEntityMapper.fromEntityToDomain(any())).willReturn(reviewSchedule)

    // When
    val exception = assertThrows(ActiveReviewScheduleAlreadyExistsException::class.java) {
      persistenceAdapter.createReviewSchedule(createReviewScheduleDto)
    }

    // Then
    assertThat(exception.prisonNumber).isEqualTo(prisonNumber)
    verify(reviewScheduleRepository).findByPrisonNumberAndScheduleStatusIn(prisonNumber, STATUSES_FOR_ACTIVE_REVIEWS)
    verify(reviewScheduleEntityMapper).fromEntityToDomain(reviewScheduleEntity)
    verifyNoMoreInteractions(reviewScheduleRepository)
    verifyNoInteractions(reviewScheduleHistoryRepository)
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
      verify(reviewScheduleHistoryRepository).save(any())
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
      verifyNoInteractions(reviewScheduleHistoryRepository)
    }
  }
}
