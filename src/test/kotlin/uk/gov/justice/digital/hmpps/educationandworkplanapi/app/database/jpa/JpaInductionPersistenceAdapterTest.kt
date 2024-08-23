package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.aValidPreviousQualifications
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.CreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.aValidCreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.aValidUpdatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aFullyPopulatedInduction
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInductionSummary
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidWorkOnRelease
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateInductionDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdateInductionDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInductionEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInductionSummaryProjection
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousQualificationsEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidWorkOnReleaseEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction.InductionEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction.PreviousQualificationsEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.PreviousQualificationsRepository
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HopingToWork as HopingToWorkDomain

@ExtendWith(MockitoExtension::class)
class JpaInductionPersistenceAdapterTest {

  @InjectMocks
  private lateinit var persistenceAdapter: JpaInductionPersistenceAdapter

  @Mock
  private lateinit var inductionRepository: InductionRepository

  @Mock
  private lateinit var inductionMapper: InductionEntityMapper

  @Mock
  private lateinit var previousQualificationsRepository: PreviousQualificationsRepository

  @Mock
  private lateinit var previousQualificationsMapper: PreviousQualificationsEntityMapper

  @Nested
  inner class CreateInduction {
    @Test
    fun `should create induction and create qualifications given qualifications do not already exist for the prisoner and createInductionDto contains qualifications`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val createInductionDto = aValidCreateInductionDto(
        prisonNumber = prisonNumber,
        previousQualifications = aValidCreatePreviousQualificationsDto(),
      )

      given(previousQualificationsRepository.findByPrisonNumber(any())).willReturn(null)

      val newPreviousQualificationsEntity = aValidPreviousQualificationsEntity(prisonNumber = prisonNumber)
      given(previousQualificationsMapper.fromCreateDtoToEntity(any())).willReturn(newPreviousQualificationsEntity)
      given(previousQualificationsRepository.saveAndFlush(any<PreviousQualificationsEntity>())).willReturn(
        newPreviousQualificationsEntity,
      )

      val inductionEntity = aValidInductionEntityWithJpaFieldsPopulated(prisonNumber = prisonNumber)
      given(inductionMapper.fromCreateDtoToEntity(any())).willReturn(inductionEntity)
      given(inductionRepository.saveAndFlush(any<InductionEntity>())).willReturn(inductionEntity)

      val expected = aFullyPopulatedInduction(
        prisonNumber = prisonNumber,
        previousQualifications = aValidPreviousQualifications(),
      )
      given(inductionMapper.fromEntityToDomain(any(), any())).willReturn(expected)

      // When
      val actual = persistenceAdapter.createInduction(createInductionDto)

      // Then
      assertThat(actual).isEqualTo(expected)
      verify(inductionMapper).fromCreateDtoToEntity(createInductionDto)
      verify(inductionRepository).saveAndFlush(inductionEntity)
      verify(previousQualificationsRepository).findByPrisonNumber(prisonNumber)
      verify(previousQualificationsMapper).fromCreateDtoToEntity(createInductionDto.previousQualifications!!)
      verify(previousQualificationsRepository).saveAndFlush(newPreviousQualificationsEntity)
      verify(inductionMapper).fromEntityToDomain(inductionEntity, newPreviousQualificationsEntity)
    }

    @Test
    fun `should create induction and update qualifications given qualifications already exist for the prisoner and createInductionDto contains qualifications`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val createInductionDto = aValidCreateInductionDto(
        prisonNumber = prisonNumber,
        previousQualifications = aValidCreatePreviousQualificationsDto(),
      )

      val previousQualificationsEntity = aValidPreviousQualificationsEntityWithJpaFieldsPopulated(prisonNumber = prisonNumber)
      given(previousQualificationsRepository.findByPrisonNumber(any())).willReturn(previousQualificationsEntity)
      given(previousQualificationsRepository.saveAndFlush(any<PreviousQualificationsEntity>())).willReturn(
        previousQualificationsEntity,
      )

      val inductionEntity = aValidInductionEntityWithJpaFieldsPopulated(prisonNumber = prisonNumber)
      given(inductionMapper.fromCreateDtoToEntity(any())).willReturn(inductionEntity)
      given(inductionRepository.saveAndFlush(any<InductionEntity>())).willReturn(inductionEntity)

      val expected = aFullyPopulatedInduction(
        prisonNumber = prisonNumber,
        previousQualifications = aValidPreviousQualifications(),
      )
      given(inductionMapper.fromEntityToDomain(any(), any())).willReturn(expected)

      // When
      val actual = persistenceAdapter.createInduction(createInductionDto)

      // Then
      assertThat(actual).isEqualTo(expected)
      verify(inductionMapper).fromCreateDtoToEntity(createInductionDto)
      verify(inductionRepository).saveAndFlush(inductionEntity)
      verify(previousQualificationsRepository).findByPrisonNumber(prisonNumber)
      verify(previousQualificationsMapper).updateExistingEntityFromDto(previousQualificationsEntity, createInductionDto.previousQualifications!!)
      verify(previousQualificationsRepository).saveAndFlush(previousQualificationsEntity)
      verify(inductionMapper).fromEntityToDomain(inductionEntity, previousQualificationsEntity)
    }

    @Test
    fun `should create induction and not create qualifications given qualifications do not already exist for the prisoner and createInductionDto does not contain qualifications`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val createInductionDto = aValidCreateInductionDto(
        prisonNumber = prisonNumber,
        previousQualifications = null,
      )

      given(previousQualificationsRepository.findByPrisonNumber(any())).willReturn(null)

      val inductionEntity = aValidInductionEntityWithJpaFieldsPopulated(prisonNumber = prisonNumber)
      given(inductionMapper.fromCreateDtoToEntity(any())).willReturn(inductionEntity)
      given(inductionRepository.saveAndFlush(any<InductionEntity>())).willReturn(inductionEntity)

      val expected = aFullyPopulatedInduction(
        prisonNumber = prisonNumber,
        previousQualifications = null,
      )
      given(inductionMapper.fromEntityToDomain(any(), eq(null))).willReturn(expected)

      // When
      val actual = persistenceAdapter.createInduction(createInductionDto)

      // Then
      assertThat(actual).isEqualTo(expected)
      verify(inductionMapper).fromCreateDtoToEntity(createInductionDto)
      verify(inductionRepository).saveAndFlush(inductionEntity)
      verify(previousQualificationsRepository).findByPrisonNumber(prisonNumber)
      verifyNoInteractions(previousQualificationsMapper)
      verifyNoMoreInteractions(previousQualificationsRepository)
      verify(inductionMapper).fromEntityToDomain(inductionEntity, null)
    }

    @Test
    fun `should create induction and delete qualifications given qualifications already exist for the prisoner and createInductionDto does not contain qualifications`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val createInductionDto = aValidCreateInductionDto(
        prisonNumber = prisonNumber,
        previousQualifications = null,
      )

      val previousQualificationsEntity = aValidPreviousQualificationsEntityWithJpaFieldsPopulated(prisonNumber = prisonNumber)
      given(previousQualificationsRepository.findByPrisonNumber(any())).willReturn(previousQualificationsEntity)

      val inductionEntity = aValidInductionEntityWithJpaFieldsPopulated(prisonNumber = prisonNumber)
      given(inductionMapper.fromCreateDtoToEntity(any())).willReturn(inductionEntity)
      given(inductionRepository.saveAndFlush(any<InductionEntity>())).willReturn(inductionEntity)

      val expected = aFullyPopulatedInduction(
        prisonNumber = prisonNumber,
        previousQualifications = null,
      )
      given(inductionMapper.fromEntityToDomain(any(), eq(null))).willReturn(expected)

      // When
      val actual = persistenceAdapter.createInduction(createInductionDto)

      // Then
      assertThat(actual).isEqualTo(expected)
      verify(inductionMapper).fromCreateDtoToEntity(createInductionDto)
      verify(inductionRepository).saveAndFlush(inductionEntity)
      verify(previousQualificationsRepository).findByPrisonNumber(prisonNumber)
      verifyNoInteractions(previousQualificationsMapper)
      verify(previousQualificationsRepository).delete(previousQualificationsEntity)
      verify(inductionMapper).fromEntityToDomain(inductionEntity, null)
    }
  }

  @Test
  fun `should get induction`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val inductionEntity = aValidInductionEntityWithJpaFieldsPopulated(prisonNumber = prisonNumber)
    val previousQualificationsEntity = aValidPreviousQualificationsEntityWithJpaFieldsPopulated(prisonNumber = prisonNumber)
    given(inductionRepository.findByPrisonNumber(any())).willReturn(inductionEntity)
    given(previousQualificationsRepository.findByPrisonNumber(any())).willReturn(previousQualificationsEntity)

    val expected = aFullyPopulatedInduction(prisonNumber = prisonNumber)
    given(inductionMapper.fromEntityToDomain(any(), any())).willReturn(expected)

    // When
    val actual = persistenceAdapter.getInduction(prisonNumber)

    // Then
    assertThat(actual).isEqualTo(expected)
    verify(inductionRepository).findByPrisonNumber(prisonNumber)
    verify(previousQualificationsRepository).findByPrisonNumber(prisonNumber)
    verify(inductionMapper).fromEntityToDomain(inductionEntity, previousQualificationsEntity)
  }

  @Test
  fun `should not get induction given induction does not exist`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    given(inductionRepository.findByPrisonNumber(any())).willReturn(null)

    // When
    val actual = persistenceAdapter.getInduction(prisonNumber)

    // Then
    assertThat(actual).isNull()
    verify(inductionRepository).findByPrisonNumber(prisonNumber)
    verifyNoInteractions(previousQualificationsRepository)
    verifyNoInteractions(inductionMapper)
  }

  @Nested
  inner class UpdateInduction {
    @Test
    fun `should update induction including qualifications given qualifications exist for the prisoner and updateInductionDto contains qualifications`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val reference = UUID.randomUUID()

      val inductionEntity = aValidInductionEntityWithJpaFieldsPopulated(
        reference = reference,
        prisonNumber = prisonNumber,
        workOnRelease = aValidWorkOnReleaseEntity(hopingToWork = HopingToWork.NO),
        createdBy = "USER1",
        updatedBy = "USER1",
      )
      given(inductionRepository.findByPrisonNumber(any())).willReturn(inductionEntity)

      val previousQualificationsEntity = aValidPreviousQualificationsEntityWithJpaFieldsPopulated(prisonNumber = prisonNumber)
      given(previousQualificationsRepository.findByPrisonNumber(any())).willReturn(previousQualificationsEntity)

      val persistedInductionEntity = aValidInductionEntityWithJpaFieldsPopulated(
        reference = reference,
        prisonNumber = prisonNumber,
        workOnRelease = aValidWorkOnReleaseEntity(hopingToWork = HopingToWork.YES),
        createdBy = "USER1",
        updatedBy = "USER2",
      )
      given(inductionRepository.saveAndFlush(any<InductionEntity>())).willReturn(persistedInductionEntity)

      val persistedPreviousQualificationsEntity = aValidPreviousQualificationsEntityWithJpaFieldsPopulated(prisonNumber = prisonNumber)
      given(previousQualificationsRepository.saveAndFlush(any<PreviousQualificationsEntity>())).willReturn(persistedPreviousQualificationsEntity)

      val expectedDomainInduction = aFullyPopulatedInduction(
        reference = reference,
        prisonNumber = prisonNumber,
        workOnRelease = aValidWorkOnRelease(hopingToWork = HopingToWorkDomain.YES),
        previousQualifications = aValidPreviousQualifications(),
        createdBy = "USER1",
        lastUpdatedBy = "USER2",
      )
      given(inductionMapper.fromEntityToDomain(any(), any())).willReturn(expectedDomainInduction)

      val updateInductionDto = aValidUpdateInductionDto(
        prisonNumber = prisonNumber,
        workOnRelease = aValidUpdateWorkOnReleaseDto(
          hopingToWork = HopingToWorkDomain.YES,
        ),
        previousQualifications = aValidUpdatePreviousQualificationsDto(),
      )

      // When
      val actual = persistenceAdapter.updateInduction(updateInductionDto)

      // Then
      assertThat(actual).isEqualTo(expectedDomainInduction)
      verify(inductionRepository).findByPrisonNumber(prisonNumber)
      verify(inductionRepository).saveAndFlush(inductionEntity)
      verify(inductionMapper).updateEntityFromDto(inductionEntity, updateInductionDto)
      verify(previousQualificationsRepository).findByPrisonNumber(prisonNumber)
      verify(previousQualificationsMapper).updateExistingEntityFromDto(previousQualificationsEntity, updateInductionDto.previousQualifications!!)
      verify(previousQualificationsRepository).saveAndFlush(previousQualificationsEntity)
      verify(inductionMapper).fromEntityToDomain(persistedInductionEntity, persistedPreviousQualificationsEntity)
    }

    @Test
    fun `should update induction and create qualifications given qualifications do not exist for the prisoner and updateInductionDto contains qualifications`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val reference = UUID.randomUUID()

      val inductionEntity = aValidInductionEntityWithJpaFieldsPopulated(
        reference = reference,
        prisonNumber = prisonNumber,
        workOnRelease = aValidWorkOnReleaseEntity(hopingToWork = HopingToWork.NO),
        createdBy = "USER1",
        updatedBy = "USER1",
      )
      given(inductionRepository.findByPrisonNumber(any())).willReturn(inductionEntity)

      given(previousQualificationsRepository.findByPrisonNumber(any())).willReturn(null)

      val persistedInductionEntity = aValidInductionEntityWithJpaFieldsPopulated(
        reference = reference,
        prisonNumber = prisonNumber,
        workOnRelease = aValidWorkOnReleaseEntity(hopingToWork = HopingToWork.YES),
        createdBy = "USER1",
        updatedBy = "USER2",
      )
      given(inductionRepository.saveAndFlush(any<InductionEntity>())).willReturn(persistedInductionEntity)

      val newPreviousQualificationsEntity = aValidPreviousQualificationsEntity(prisonNumber = prisonNumber)
      given(previousQualificationsMapper.fromCreateDtoToEntity(any())).willReturn(newPreviousQualificationsEntity)
      val persistedPreviousQualificationsEntity = aValidPreviousQualificationsEntityWithJpaFieldsPopulated(prisonNumber = prisonNumber)
      given(previousQualificationsRepository.saveAndFlush(any<PreviousQualificationsEntity>())).willReturn(persistedPreviousQualificationsEntity)

      val expectedDomainInduction = aFullyPopulatedInduction(
        reference = reference,
        prisonNumber = prisonNumber,
        workOnRelease = aValidWorkOnRelease(hopingToWork = HopingToWorkDomain.YES),
        previousQualifications = aValidPreviousQualifications(),
        createdBy = "USER1",
        lastUpdatedBy = "USER2",
      )
      given(inductionMapper.fromEntityToDomain(any(), any())).willReturn(expectedDomainInduction)

      val updateInductionDto = aValidUpdateInductionDto(
        prisonNumber = prisonNumber,
        workOnRelease = aValidUpdateWorkOnReleaseDto(
          hopingToWork = HopingToWorkDomain.YES,
        ),
        previousQualifications = aValidUpdatePreviousQualificationsDto(),
      )

      val expectedCreatePreviousQualificationsDto = CreatePreviousQualificationsDto(
        prisonNumber,
        updateInductionDto.previousQualifications!!.educationLevel!!,
        updateInductionDto.previousQualifications!!.qualifications,
        updateInductionDto.previousQualifications!!.prisonId,
      )

      // When
      val actual = persistenceAdapter.updateInduction(updateInductionDto)

      // Then
      assertThat(actual).isEqualTo(expectedDomainInduction)
      verify(inductionRepository).findByPrisonNumber(prisonNumber)
      verify(inductionRepository).saveAndFlush(inductionEntity)
      verify(inductionMapper).updateEntityFromDto(inductionEntity, updateInductionDto)
      verify(previousQualificationsRepository).findByPrisonNumber(prisonNumber)
      verify(previousQualificationsMapper).fromCreateDtoToEntity(expectedCreatePreviousQualificationsDto)
      verify(previousQualificationsRepository).saveAndFlush(newPreviousQualificationsEntity)
      verify(inductionMapper).fromEntityToDomain(persistedInductionEntity, persistedPreviousQualificationsEntity)
    }

    @Test
    fun `should update induction without adding or updating qualifications given qualifications do not exist for the prisoner and updateInductionDto does not contain qualifications`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val reference = UUID.randomUUID()

      val inductionEntity = aValidInductionEntityWithJpaFieldsPopulated(
        reference = reference,
        prisonNumber = prisonNumber,
        workOnRelease = aValidWorkOnReleaseEntity(hopingToWork = HopingToWork.NO),
        createdBy = "USER1",
        updatedBy = "USER1",
      )
      given(inductionRepository.findByPrisonNumber(any())).willReturn(inductionEntity)

      given(previousQualificationsRepository.findByPrisonNumber(any())).willReturn(null)

      val persistedInductionEntity = aValidInductionEntityWithJpaFieldsPopulated(
        reference = reference,
        prisonNumber = prisonNumber,
        workOnRelease = aValidWorkOnReleaseEntity(hopingToWork = HopingToWork.YES),
        createdBy = "USER1",
        updatedBy = "USER2",
      )
      given(inductionRepository.saveAndFlush(any<InductionEntity>())).willReturn(persistedInductionEntity)

      val expectedDomainInduction = aFullyPopulatedInduction(
        reference = reference,
        prisonNumber = prisonNumber,
        workOnRelease = aValidWorkOnRelease(hopingToWork = HopingToWorkDomain.YES),
        previousQualifications = null,
        createdBy = "USER1",
        lastUpdatedBy = "USER2",
      )
      given(inductionMapper.fromEntityToDomain(any(), eq(null))).willReturn(expectedDomainInduction)

      val updateInductionDto = aValidUpdateInductionDto(
        prisonNumber = prisonNumber,
        workOnRelease = aValidUpdateWorkOnReleaseDto(
          hopingToWork = HopingToWorkDomain.YES,
        ),
        previousQualifications = null,
      )

      // When
      val actual = persistenceAdapter.updateInduction(updateInductionDto)

      // Then
      assertThat(actual).isEqualTo(expectedDomainInduction)
      verify(inductionRepository).findByPrisonNumber(prisonNumber)
      verify(inductionRepository).saveAndFlush(inductionEntity)
      verify(inductionMapper).updateEntityFromDto(inductionEntity, updateInductionDto)
      verify(previousQualificationsRepository).findByPrisonNumber(prisonNumber)
      verifyNoInteractions(previousQualificationsMapper)
      verifyNoMoreInteractions(previousQualificationsRepository)
      verify(inductionMapper).fromEntityToDomain(persistedInductionEntity, null)
    }

    @Test
    fun `should update induction without adding or updating qualifications given qualifications exist for the prisoner and updateInductionDto does not contain qualifications`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val reference = UUID.randomUUID()

      val inductionEntity = aValidInductionEntityWithJpaFieldsPopulated(
        reference = reference,
        prisonNumber = prisonNumber,
        workOnRelease = aValidWorkOnReleaseEntity(hopingToWork = HopingToWork.NO),
        createdBy = "USER1",
        updatedBy = "USER1",
      )
      given(inductionRepository.findByPrisonNumber(any())).willReturn(inductionEntity)

      val previousQualificationsEntity = aValidPreviousQualificationsEntityWithJpaFieldsPopulated(prisonNumber = prisonNumber)
      given(previousQualificationsRepository.findByPrisonNumber(any())).willReturn(previousQualificationsEntity)

      val persistedInductionEntity = aValidInductionEntityWithJpaFieldsPopulated(
        reference = reference,
        prisonNumber = prisonNumber,
        workOnRelease = aValidWorkOnReleaseEntity(hopingToWork = HopingToWork.YES),
        createdBy = "USER1",
        updatedBy = "USER2",
      )
      given(inductionRepository.saveAndFlush(any<InductionEntity>())).willReturn(persistedInductionEntity)

      val expectedDomainInduction = aFullyPopulatedInduction(
        reference = reference,
        prisonNumber = prisonNumber,
        workOnRelease = aValidWorkOnRelease(hopingToWork = HopingToWorkDomain.YES),
        previousQualifications = aValidPreviousQualifications(),
        createdBy = "USER1",
        lastUpdatedBy = "USER2",
      )
      given(inductionMapper.fromEntityToDomain(any(), any())).willReturn(expectedDomainInduction)

      val updateInductionDto = aValidUpdateInductionDto(
        prisonNumber = prisonNumber,
        workOnRelease = aValidUpdateWorkOnReleaseDto(
          hopingToWork = HopingToWorkDomain.YES,
        ),
        previousQualifications = null,
      )

      // When
      val actual = persistenceAdapter.updateInduction(updateInductionDto)

      // Then
      assertThat(actual).isEqualTo(expectedDomainInduction)
      verify(inductionRepository).findByPrisonNumber(prisonNumber)
      verify(inductionRepository).saveAndFlush(inductionEntity)
      verify(inductionMapper).updateEntityFromDto(inductionEntity, updateInductionDto)
      verify(previousQualificationsRepository).findByPrisonNumber(prisonNumber)
      verifyNoInteractions(previousQualificationsMapper)
      verifyNoMoreInteractions(previousQualificationsRepository)
      verify(inductionMapper).fromEntityToDomain(persistedInductionEntity, previousQualificationsEntity)
    }

    @Test
    fun `should not update induction given induction does not exist`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val updateInductionDto = aValidUpdateInductionDto(prisonNumber = prisonNumber)
      given(inductionRepository.findByPrisonNumber(any())).willReturn(null)

      // When
      val actual = persistenceAdapter.updateInduction(updateInductionDto)

      // Then
      assertThat(actual).isNull()
      verifyNoInteractions(inductionMapper)
      verifyNoInteractions(previousQualificationsRepository)
      verifyNoInteractions(previousQualificationsMapper)
    }
  }

  @Test
  fun `should retrieve Induction Summaries`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val prisonNumbers = listOf(prisonNumber)
    val inductionSummaryProjections = listOf(aValidInductionSummaryProjection(prisonNumber = prisonNumber))
    val expectedInductionSummaries = listOf(aValidInductionSummary(prisonNumber = prisonNumber))
    given(inductionRepository.findByPrisonNumberIn(any())).willReturn(inductionSummaryProjections)
    given(inductionMapper.fromEntitySummariesToDomainSummaries(any())).willReturn(expectedInductionSummaries)

    // When
    val actual = persistenceAdapter.getInductionSummaries(prisonNumbers)

    // Then
    assertThat(actual).isEqualTo(expectedInductionSummaries)
    verify(inductionRepository).findByPrisonNumberIn(prisonNumbers)
    verify(inductionMapper).fromEntitySummariesToDomainSummaries(inductionSummaryProjections)
  }
}
