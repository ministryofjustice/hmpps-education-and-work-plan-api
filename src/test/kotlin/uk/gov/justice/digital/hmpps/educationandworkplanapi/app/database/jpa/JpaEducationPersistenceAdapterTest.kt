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
import org.mockito.kotlin.verifyNoMoreInteractions
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.aValidReference
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.aValidPreviousQualifications
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.aValidCreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.aValidUpdatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousQualificationsEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction.PreviousQualificationsEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.PreviousQualificationsRepository
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class JpaEducationPersistenceAdapterTest {

  @InjectMocks
  private lateinit var persistenceAdapter: JpaEducationPersistenceAdapter

  @Mock
  private lateinit var previousQualificationsRepository: PreviousQualificationsRepository

  @Mock
  private lateinit var previousQualificationsMapper: PreviousQualificationsEntityMapper

  @Nested
  inner class GetPreviousQualifications {
    @Test
    fun `should get previous qualifications`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val previousQualificationsEntity =
        aValidPreviousQualificationsEntityWithJpaFieldsPopulated(prisonNumber = prisonNumber)
      given(previousQualificationsRepository.findByPrisonNumber(any())).willReturn(previousQualificationsEntity)

      val expected = aValidPreviousQualifications(prisonNumber = prisonNumber)
      given(previousQualificationsMapper.fromEntityToDomain(any())).willReturn(expected)

      // When
      val actual = persistenceAdapter.getPreviousQualifications(prisonNumber)

      // Then
      assertThat(actual).isEqualTo(expected)
      verify(previousQualificationsRepository).findByPrisonNumber(prisonNumber)
      verify(previousQualificationsMapper).fromEntityToDomain(previousQualificationsEntity)
    }

    @Test
    fun `should not get induction given induction does not exist`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      given(previousQualificationsRepository.findByPrisonNumber(any())).willReturn(null)

      // When
      val actual = persistenceAdapter.getPreviousQualifications(prisonNumber)

      // Then
      assertThat(actual).isNull()
      verify(previousQualificationsRepository).findByPrisonNumber(prisonNumber)
      verifyNoInteractions(previousQualificationsMapper)
    }
  }

  @Nested
  inner class CreatePreviousQualifications {
    @Test
    fun `should create previous qualifications`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val createPreviousQualificationsDto = aValidCreatePreviousQualificationsDto(prisonNumber = prisonNumber)

      val newPreviousQualificationsEntity = aValidPreviousQualificationsEntity(prisonNumber = prisonNumber)
      given(previousQualificationsMapper.fromCreateDtoToEntity(any())).willReturn(newPreviousQualificationsEntity)

      val previousQualificationsEntity =
        aValidPreviousQualificationsEntityWithJpaFieldsPopulated(prisonNumber = prisonNumber)
      given(previousQualificationsRepository.saveAndFlush(any<PreviousQualificationsEntity>())).willReturn(
        previousQualificationsEntity,
      )

      val expected = aValidPreviousQualifications(prisonNumber = prisonNumber)
      given(previousQualificationsMapper.fromEntityToDomain(any())).willReturn(expected)

      // When
      val actual = persistenceAdapter.createPreviousQualifications(createPreviousQualificationsDto)

      // Then
      assertThat(actual).isEqualTo(expected)
      verify(previousQualificationsMapper).fromCreateDtoToEntity(createPreviousQualificationsDto)
      verify(previousQualificationsMapper).fromEntityToDomain(previousQualificationsEntity)
    }
  }

  @Nested
  inner class UpdatePreviousQualifications {
    @Test
    fun `should update prisoner's previous qualifications given previous qualifications exists`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val reference = aValidReference()
      val previousQualificationsEntity = aValidPreviousQualificationsEntity(
        reference = reference,
        prisonNumber = prisonNumber,
      )
      given(previousQualificationsRepository.findByPrisonNumber(any())).willReturn(previousQualificationsEntity)

      val updatePreviousQualificationsDto = aValidUpdatePreviousQualificationsDto(
        reference = reference,
        prisonNumber = prisonNumber,
      )

      val persistedEntity = aValidPreviousQualificationsEntity(
        reference = reference,
        prisonNumber = prisonNumber,
      )
      given(previousQualificationsRepository.saveAndFlush(any<PreviousQualificationsEntity>())).willReturn(persistedEntity)

      val expectedPreviousQualifications = aValidPreviousQualifications(
        reference = reference,
        prisonNumber = prisonNumber,
      )
      given(previousQualificationsMapper.fromEntityToDomain(any())).willReturn(expectedPreviousQualifications)

      // When
      val actual = persistenceAdapter.updatePreviousQualifications(updatePreviousQualificationsDto)

      // Then
      assertThat(actual).isEqualTo(expectedPreviousQualifications)
      verify(previousQualificationsRepository).findByPrisonNumber(prisonNumber)
      verify(previousQualificationsMapper).updateExistingEntityFromDto(previousQualificationsEntity, updatePreviousQualificationsDto)
      verify(previousQualificationsRepository).saveAndFlush(previousQualificationsEntity)
      verify(previousQualificationsMapper).fromEntityToDomain(persistedEntity)
    }
  }

  @Test
  fun `should not update prisoner's previous qualifications given prisoner's previous qualifications does not exist`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val reference = aValidReference()
    given(previousQualificationsRepository.findByPrisonNumber(any())).willReturn(null)

    val updatePreviousQualificationsDto = aValidUpdatePreviousQualificationsDto(
      reference = reference,
      prisonNumber = prisonNumber,
    )

    // When
    val actual = persistenceAdapter.updatePreviousQualifications(updatePreviousQualificationsDto)

    // Then
    assertThat(actual).isNull()
    verify(previousQualificationsRepository).findByPrisonNumber(prisonNumber)
    verifyNoMoreInteractions(previousQualificationsRepository)
    verifyNoInteractions(previousQualificationsMapper)
  }

  @Test
  fun `should not update prisoner's previous qualifications given prisoner's previous qualifications exists but with a different reference`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val previousQualificationsEntity = aValidPreviousQualificationsEntity(
      reference = UUID.randomUUID(),
      prisonNumber = prisonNumber,
    )
    given(previousQualificationsRepository.findByPrisonNumber(any())).willReturn(previousQualificationsEntity)

    val reference = aValidReference()

    val updatePreviousQualificationsDto = aValidUpdatePreviousQualificationsDto(
      reference = reference,
      prisonNumber = prisonNumber,
    )

    // When
    val actual = persistenceAdapter.updatePreviousQualifications(updatePreviousQualificationsDto)

    // Then
    assertThat(actual).isNull()
    verify(previousQualificationsRepository).findByPrisonNumber(prisonNumber)
    verifyNoMoreInteractions(previousQualificationsRepository)
    verifyNoInteractions(previousQualificationsMapper)
  }
}
