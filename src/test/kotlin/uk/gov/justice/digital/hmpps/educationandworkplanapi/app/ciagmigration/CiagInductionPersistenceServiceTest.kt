package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration

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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.aValidInductionMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.repository.InductionMigrationRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.mapper.InductionMigrationMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.aValidCiagInductionResponse

@ExtendWith(MockitoExtension::class)
class CiagInductionPersistenceServiceTest {

  @Mock
  private lateinit var inductionMigrationRepository: InductionMigrationRepository

  @Mock
  private lateinit var inductionMigrationMapper: InductionMigrationMapper

  @InjectMocks
  private lateinit var inductionPersistenceService: CiagInductionPersistenceService

  @Test
  fun `should save induction given induction has not been imported`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val ciagInduction = aValidCiagInductionResponse(offenderId = prisonNumber)
    val inductionEntity = aValidInductionMigrationEntity(prisonNumber = prisonNumber)
    given(inductionMigrationRepository.findByPrisonNumber(any())).willReturn(null)
    given(inductionMigrationMapper.toInductionMigrationEntity(any())).willReturn(inductionEntity)

    // When
    inductionPersistenceService.saveInduction(ciagInduction)

    // Then
    verify(inductionMigrationRepository).findByPrisonNumber(prisonNumber)
    verify(inductionMigrationMapper).toInductionMigrationEntity(ciagInduction)
    verify(inductionMigrationRepository).save(inductionEntity)
  }

  @Test
  fun `should not save induction given induction has been imported`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val ciagInduction = aValidCiagInductionResponse(offenderId = prisonNumber)

    given(inductionMigrationRepository.findByPrisonNumber(any())).willReturn(aValidInductionMigrationEntity())

    // When
    inductionPersistenceService.saveInduction(ciagInduction)

    // Then
    verify(inductionMigrationRepository).findByPrisonNumber(prisonNumber)
    verifyNoInteractions(inductionMigrationMapper)
  }
}
