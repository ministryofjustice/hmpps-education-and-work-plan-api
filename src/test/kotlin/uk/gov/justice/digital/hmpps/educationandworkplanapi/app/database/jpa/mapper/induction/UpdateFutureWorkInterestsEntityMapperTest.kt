package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidWorkInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidFutureWorkInterestsEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidWorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkInterestType as WorkInterestTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkInterestType as WorkInterestTypeEntity

class UpdateFutureWorkInterestsEntityMapperTest {

  private val mapper = FutureWorkInterestsEntityMapper(
    WorkInterestEntityMapper(),
    InductionEntityListManager(),
  )

  @Test
  fun `should update existing work interests`() {
    // Given
    val workInterestReference = UUID.randomUUID()
    val existingEntity = aValidWorkInterestEntity(
      reference = workInterestReference,
      workType = WorkInterestTypeEntity.OTHER,
      workTypeOther = "Any job I can get",
      role = "Any role",
    )
    val futureWorkInterestsReference = UUID.randomUUID()
    val existingFutureWorkInterestsEntity = aValidFutureWorkInterestsEntityWithJpaFieldsPopulated(
      reference = futureWorkInterestsReference,
      interests = mutableListOf(existingEntity),
    )

    val updatedInterest = aValidWorkInterest(
      workType = WorkInterestTypeDomain.OTHER,
      workTypeOther = "Any job that pays ok",
      role = null,
    )
    val updatedInterestsDto = aValidUpdateFutureWorkInterestsDto(
      reference = futureWorkInterestsReference,
      interests = listOf(updatedInterest),
      prisonId = "MDI",
    )

    val expectedEntity = existingFutureWorkInterestsEntity.copy(
      interests = mutableListOf(
        existingEntity.copy(
          workType = WorkInterestTypeEntity.OTHER,
          workTypeOther = "Any job that pays ok",
          role = null,
        ),
      ),
      createdAtPrison = "BXI",
      updatedAtPrison = "MDI",
    )

    // When
    mapper.updateExistingEntityFromDto(existingFutureWorkInterestsEntity, updatedInterestsDto)

    // Then
    assertThat(existingFutureWorkInterestsEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
  }

  @Test
  fun `should add new work interest`() {
    // Given
    val workInterestReference = UUID.randomUUID()
    val existingEntity = aValidWorkInterestEntity(
      reference = workInterestReference,
      workType = WorkInterestTypeEntity.OTHER,
      workTypeOther = "Any job I can get",
      role = "Any role",
    )
    val futureWorkInterestsReference = UUID.randomUUID()
    val existingFutureWorkInterestsEntity = aValidFutureWorkInterestsEntityWithJpaFieldsPopulated(
      reference = futureWorkInterestsReference,
      interests = mutableListOf(existingEntity),
    )

    val unchangedWorkInterestDomain = aValidWorkInterest(
      workType = WorkInterestTypeDomain.OTHER,
      workTypeOther = "Any job I can get",
      role = "Any role",
    )
    val newWorkInterestDomain = aValidWorkInterest(
      workType = WorkInterestTypeDomain.SPORTS,
      workTypeOther = null,
      role = "Professional football player",
    )
    val updatedFutureWorkInterestsDto = aValidUpdateFutureWorkInterestsDto(
      reference = futureWorkInterestsReference,
      interests = listOf(unchangedWorkInterestDomain, newWorkInterestDomain),
      prisonId = "MDI",
    )

    val expectedEntity = existingFutureWorkInterestsEntity.copy(
      interests = mutableListOf(
        aValidWorkInterestEntity(
          workType = WorkInterestTypeEntity.OTHER,
          workTypeOther = "Any job I can get",
          role = "Any role",
        ),
        aValidWorkInterestEntity(
          workType = WorkInterestTypeEntity.SPORTS,
          workTypeOther = null,
          role = "Professional football player",
        ),
      ),
      createdAtPrison = "BXI",
      updatedAtPrison = "MDI",
    )

    // When
    mapper.updateExistingEntityFromDto(existingFutureWorkInterestsEntity, updatedFutureWorkInterestsDto)

    // Then
    assertThat(existingFutureWorkInterestsEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
  }

  @Test
  fun `should remove work interests`() {
    // Given
    val workInterestReference = UUID.randomUUID()
    val firstWorkInterestEntity = aValidWorkInterestEntity(
      reference = workInterestReference,
      workType = WorkInterestTypeEntity.OTHER,
      workTypeOther = "Any job I can get",
      role = "Any role",
    )
    val secondWorkInterestEntity = aValidWorkInterestEntity(
      workType = WorkInterestTypeEntity.CONSTRUCTION,
      workTypeOther = null,
      role = "Bricklaying",
    )
    val futureWorkInterestsReference = UUID.randomUUID()
    val existingFutureWorkInterestsEntity = aValidFutureWorkInterestsEntityWithJpaFieldsPopulated(
      reference = futureWorkInterestsReference,
      interests = mutableListOf(firstWorkInterestEntity, secondWorkInterestEntity),
    )

    val updatedFutureWorkInterestsDto = aValidUpdateFutureWorkInterestsDto(
      reference = futureWorkInterestsReference,
      interests = listOf(
        aValidWorkInterest(
          // only first interest above included
          workType = WorkInterestTypeDomain.OTHER,
          workTypeOther = "Any job I can get",
          role = "Any role",
        ),
      ),
      prisonId = "MDI",
    )

    val expectedEntity = existingFutureWorkInterestsEntity.copy(
      interests = mutableListOf(firstWorkInterestEntity),
      createdAtPrison = "BXI",
      updatedAtPrison = "MDI",
    )

    // When
    mapper.updateExistingEntityFromDto(existingFutureWorkInterestsEntity, updatedFutureWorkInterestsDto)

    // Then
    assertThat(existingFutureWorkInterestsEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
  }
}
