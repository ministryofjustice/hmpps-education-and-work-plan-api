package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInductionEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInductionSummaryProjection
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidWorkOnReleaseEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction.InductionEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidInduction
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidInductionSummary
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidWorkOnRelease
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreateInductionDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidUpdateInductionDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidUpdateWorkOnReleaseDto
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.HopingToWork as HopingToWorkDomain

@ExtendWith(MockitoExtension::class)
class JpaInductionPersistenceAdapterTest {

  @InjectMocks
  private lateinit var persistenceAdapter: JpaInductionPersistenceAdapter

  @Mock
  private lateinit var inductionRepository: InductionRepository

  @Mock
  private lateinit var inductionMapper: InductionEntityMapper

  @Test
  fun `should create induction`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createInductionDto = aValidCreateInductionDto(prisonNumber = prisonNumber)
    val inductionEntity = aValidInductionEntity(prisonNumber = prisonNumber)
    val expected = aValidInduction(prisonNumber = prisonNumber)
    given(inductionMapper.fromCreateDtoToEntity(any())).willReturn(inductionEntity)
    given(inductionRepository.saveAndFlush(any<InductionEntity>())).willReturn(inductionEntity)
    given(inductionMapper.fromEntityToDomain(any())).willReturn(expected)

    // When
    val actual = persistenceAdapter.createInduction(createInductionDto)

    // Then
    assertThat(actual).isEqualTo(expected)
    verify(inductionMapper).fromCreateDtoToEntity(createInductionDto)
    verify(inductionRepository).saveAndFlush(inductionEntity)
    verify(inductionMapper).fromEntityToDomain(inductionEntity)
  }

  @Test
  fun `should get induction`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val inductionEntity = aValidInductionEntity(prisonNumber = prisonNumber)
    val expected = aValidInduction(prisonNumber = prisonNumber)
    given(inductionRepository.findByPrisonNumber(any())).willReturn(inductionEntity)
    given(inductionMapper.fromEntityToDomain(any())).willReturn(expected)

    // When
    val actual = persistenceAdapter.getInduction(prisonNumber)

    // Then
    assertThat(actual).isEqualTo(expected)
    verify(inductionRepository).findByPrisonNumber(prisonNumber)
    verify(inductionMapper).fromEntityToDomain(inductionEntity)
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
    verifyNoInteractions(inductionMapper)
  }

  @Test
  fun `should update induction`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val reference = UUID.randomUUID()

    val inductionEntity = aValidInductionEntity(
      reference = reference,
      prisonNumber = prisonNumber,
      workOnRelease = aValidWorkOnReleaseEntity(hopingToWork = HopingToWork.NO),
      createdBy = "USER1",
      updatedBy = "USER1",
    )
    given(inductionRepository.findByPrisonNumber(any())).willReturn(inductionEntity)

    val persistedInductionEntity = aValidInductionEntity(
      reference = reference,
      prisonNumber = prisonNumber,
      workOnRelease = aValidWorkOnReleaseEntity(hopingToWork = HopingToWork.YES),
      createdBy = "USER1",
      updatedBy = "USER2",
    )
    given(inductionRepository.saveAndFlush(any<InductionEntity>())).willReturn(persistedInductionEntity)

    val expectedDomainInduction = aValidInduction(
      reference = reference,
      prisonNumber = prisonNumber,
      workOnRelease = aValidWorkOnRelease(hopingToWork = HopingToWorkDomain.YES),
      createdBy = "USER1",
      lastUpdatedBy = "USER2",
    )
    given(inductionMapper.fromEntityToDomain(any())).willReturn(expectedDomainInduction)

    val updateInductionDto = aValidUpdateInductionDto(
      prisonNumber = prisonNumber,
      workOnRelease = aValidUpdateWorkOnReleaseDto(
        hopingToWork = HopingToWorkDomain.YES,
      ),
    )

    // When
    val actual = persistenceAdapter.updateInduction(updateInductionDto)

    // Then
    assertThat(actual).isEqualTo(expectedDomainInduction)
    verify(inductionRepository).findByPrisonNumber(prisonNumber)
    verify(inductionRepository).saveAndFlush(inductionEntity)
    verify(inductionMapper).updateEntityFromDto(inductionEntity, updateInductionDto)
    verify(inductionMapper).fromEntityToDomain(persistedInductionEntity)
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
