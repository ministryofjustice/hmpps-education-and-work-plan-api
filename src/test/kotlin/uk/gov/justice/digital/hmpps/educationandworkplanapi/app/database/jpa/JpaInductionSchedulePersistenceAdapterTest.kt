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
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleHistoryEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aPersistedInductionScheduleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.anUnPersistedInductionScheduleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction.InductionScheduleEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionScheduleHistoryRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionScheduleRepository

@ExtendWith(MockitoExtension::class)
class JpaInductionSchedulePersistenceAdapterTest {

  @InjectMocks
  private lateinit var persistenceAdapter: JpaInductionSchedulePersistenceAdapter

  @Mock
  private lateinit var inductionScheduleRepository: InductionScheduleRepository

  @Mock
  private lateinit var inductionScheduleHistoryRepository: InductionScheduleHistoryRepository

  @Mock
  private lateinit var inductionScheduleEntityMapper: InductionScheduleEntityMapper

  @Nested
  inner class GetInductionSchedule {
    @Test
    fun `should get induction schedule`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()

      val inductionScheduleEntity = aPersistedInductionScheduleEntity(prisonNumber = prisonNumber)
      given(inductionScheduleRepository.findByPrisonNumber(any())).willReturn(inductionScheduleEntity)

      val expectedInductionSchedule = aValidInductionSchedule(prisonNumber = prisonNumber)
      given(inductionScheduleEntityMapper.fromEntityToDomain(any())).willReturn(expectedInductionSchedule)

      // When
      val actual = persistenceAdapter.getInductionSchedule(prisonNumber)

      // Then
      assertThat(actual).isEqualTo(expectedInductionSchedule)
      verify(inductionScheduleRepository).findByPrisonNumber(prisonNumber)
      verify(inductionScheduleEntityMapper).fromEntityToDomain(inductionScheduleEntity)
    }

    @Test
    fun `should not get induction schedule given induction schedule does not exist for prisoner`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()

      given(inductionScheduleRepository.findByPrisonNumber(any())).willReturn(null)

      // When
      val actual = persistenceAdapter.getInductionSchedule(prisonNumber)

      // Then
      assertThat(actual).isNull()
      verify(inductionScheduleRepository).findByPrisonNumber(prisonNumber)
      verifyNoInteractions(inductionScheduleEntityMapper)
    }
  }

  @Nested
  inner class CreateInductionSchedule {
    @Test
    fun `should create induction schedule`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()

      val createInductionScheduleDto = aValidCreateInductionScheduleDto(prisonNumber = prisonNumber)

      val newInductionScheduleEntity = anUnPersistedInductionScheduleEntity(prisonNumber = prisonNumber)
      given(inductionScheduleEntityMapper.fromCreateDtoToEntity(any())).willReturn(newInductionScheduleEntity)

      val persistedInductionScheduleEntity = aPersistedInductionScheduleEntity(prisonNumber = prisonNumber)
      given(inductionScheduleRepository.saveAndFlush(any<InductionScheduleEntity>())).willReturn(persistedInductionScheduleEntity)

      val expectedInductionSchedule = aValidInductionSchedule(prisonNumber = prisonNumber)
      given(inductionScheduleEntityMapper.fromEntityToDomain(any())).willReturn(expectedInductionSchedule)

      // When
      val actual = persistenceAdapter.createInductionSchedule(createInductionScheduleDto)

      // Then
      assertThat(actual).isEqualTo(expectedInductionSchedule)
      verify(inductionScheduleEntityMapper).fromCreateDtoToEntity(createInductionScheduleDto)
      verify(inductionScheduleRepository).saveAndFlush(newInductionScheduleEntity)
      verify(inductionScheduleHistoryRepository).save(any<InductionScheduleHistoryEntity>())
      verify(inductionScheduleEntityMapper).fromEntityToDomain(persistedInductionScheduleEntity)
    }
  }
}
