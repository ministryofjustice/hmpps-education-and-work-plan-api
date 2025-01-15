package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidWorkExperience
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousWorkExperiencesEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidWorkExperienceEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkExperienceType as WorkExperienceTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkExperienceType as WorkExperienceTypeEntity

class UpdatePreviousWorkExperiencesEntityMapperTest {

  private val mapper = PreviousWorkExperiencesEntityMapper(WorkExperienceEntityMapper(), InductionEntityListManager())

  @Test
  fun `should update existing work experiences`() {
    // Given
    val workExperienceReference = UUID.randomUUID()
    val existingEntity = aValidWorkExperienceEntity(
      reference = workExperienceReference,
      experienceType = WorkExperienceTypeEntity.OTHER,
      experienceTypeOther = "Warehouse work",
      role = "Chief Forklift Truck Driver",
      details = "Forward, pick stuff up, reverse etc",
    )
    val previousWorkExperiencesReference = UUID.randomUUID()
    val existingPreviousWorkExperiencesEntity = aValidPreviousWorkExperiencesEntityWithJpaFieldsPopulated(
      reference = previousWorkExperiencesReference,
      experiences = mutableListOf(existingEntity),
    )

    val updatedExperience = aValidWorkExperience(
      experienceType = WorkExperienceTypeDomain.OTHER,
      experienceTypeOther = "Building work",
      role = "Bricklaying",
      details = "Various types of bricklaying",
    )
    val updatedExperiencesDto = aValidUpdatePreviousWorkExperiencesDto(
      reference = previousWorkExperiencesReference,
      experiences = listOf(updatedExperience),
      prisonId = "MDI",
    )

    val expectedEntity = existingPreviousWorkExperiencesEntity.copy(
      experiences = mutableListOf(
        existingEntity.copy(
          experienceType = WorkExperienceTypeEntity.OTHER,
          experienceTypeOther = "Building work",
          role = "Bricklaying",
          details = "Various types of bricklaying",
        ),
      ),
      createdAtPrison = "BXI",
      updatedAtPrison = "MDI",
    )

    // When
    mapper.updateExistingEntityFromDto(existingPreviousWorkExperiencesEntity, updatedExperiencesDto)

    // Then
    assertThat(existingPreviousWorkExperiencesEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
  }

  @Test
  fun `should not update existing work experiences given no changes`() {
    // Given
    val workExperienceReference = UUID.randomUUID()
    val existingEntity = aValidWorkExperienceEntity(
      reference = workExperienceReference,
      experienceType = WorkExperienceTypeEntity.OTHER,
      experienceTypeOther = "Warehouse work",
      role = "Chief Forklift Truck Driver",
      details = "Forward, pick stuff up, reverse etc",
    )
    val previousWorkExperiencesReference = UUID.randomUUID()
    val existingPreviousWorkExperiencesEntity = aValidPreviousWorkExperiencesEntityWithJpaFieldsPopulated(
      reference = previousWorkExperiencesReference,
      experiences = mutableListOf(existingEntity),
    )

    val updatedExperiencesDto = aValidUpdatePreviousWorkExperiencesDto(
      reference = previousWorkExperiencesReference,
      experiences = listOf(
        aValidWorkExperience(
          experienceType = WorkExperienceTypeDomain.OTHER,
          experienceTypeOther = "Warehouse work",
          role = "Chief Forklift Truck Driver",
          details = "Forward, pick stuff up, reverse etc",
        ),
      ),
    )

    val expectedEntity = existingPreviousWorkExperiencesEntity.copy()

    // When
    mapper.updateExistingEntityFromDto(existingPreviousWorkExperiencesEntity, updatedExperiencesDto)

    // Then
    assertThat(existingPreviousWorkExperiencesEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
  }

  @Test
  fun `should add new work experiences`() {
    // Given
    val workExperienceReference = UUID.randomUUID()
    val existingEntity = aValidWorkExperienceEntity(
      reference = workExperienceReference,
      experienceType = WorkExperienceTypeEntity.OTHER,
      experienceTypeOther = "Warehouse work",
      role = "Chief Forklift Truck Driver",
      details = "Forward, pick stuff up, reverse etc",
    )
    val previousWorkExperiencesReference = UUID.randomUUID()
    val existingPreviousWorkExperiencesEntity = aValidPreviousWorkExperiencesEntityWithJpaFieldsPopulated(
      reference = previousWorkExperiencesReference,
      experiences = mutableListOf(existingEntity),
    )

    val existingExperience = aValidWorkExperience(
      experienceType = WorkExperienceTypeDomain.OTHER,
      experienceTypeOther = "Warehouse work",
      role = "Chief Forklift Truck Driver",
      details = "Forward, pick stuff up, reverse etc",
    )
    val newExperience = aValidWorkExperience(
      experienceType = WorkExperienceTypeDomain.CONSTRUCTION,
      experienceTypeOther = null,
      role = "Bricklaying",
      details = "Various types of bricklaying",
    )
    val updateExperiencesDto = aValidUpdatePreviousWorkExperiencesDto(
      reference = workExperienceReference,
      experiences = listOf(existingExperience, newExperience),
      prisonId = "MDI",
    )

    val expectedEntity = existingPreviousWorkExperiencesEntity.copy(
      experiences = mutableListOf(
        aValidWorkExperienceEntity(
          experienceType = WorkExperienceTypeEntity.OTHER,
          experienceTypeOther = "Warehouse work",
          role = "Chief Forklift Truck Driver",
          details = "Forward, pick stuff up, reverse etc",
        ),
        aValidWorkExperienceEntity(
          experienceType = WorkExperienceTypeEntity.CONSTRUCTION,
          experienceTypeOther = null,
          role = "Bricklaying",
          details = "Various types of bricklaying",
        ),
      ),
      createdAtPrison = "BXI",
      updatedAtPrison = "MDI",
    )

    // When
    mapper.updateExistingEntityFromDto(existingPreviousWorkExperiencesEntity, updateExperiencesDto)

    // Then
    assertThat(existingPreviousWorkExperiencesEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
  }

  @Test
  fun `should remove work experiences`() {
    // Given
    val workExperienceReference = UUID.randomUUID()
    val firstWorkExperienceEntity = aValidWorkExperienceEntity(
      reference = workExperienceReference,
      experienceType = WorkExperienceTypeEntity.OTHER,
      experienceTypeOther = "Warehouse work",
      role = "Chief Forklift Truck Driver",
      details = "Forward, pick stuff up, reverse etc",
    )
    val secondWorkExperienceEntity = aValidWorkExperienceEntity(
      experienceType = WorkExperienceTypeEntity.CONSTRUCTION,
      experienceTypeOther = null,
      role = "Bricklaying",
      details = "Various types of bricklaying",
    )
    val previousWorkExperiencesReference = UUID.randomUUID()
    val existingPreviousWorkExperiencesEntity = aValidPreviousWorkExperiencesEntityWithJpaFieldsPopulated(
      reference = previousWorkExperiencesReference,
      experiences = mutableListOf(firstWorkExperienceEntity, secondWorkExperienceEntity),
    )

    val updatePreviousWorkExperiencesDto = aValidUpdatePreviousWorkExperiencesDto(
      reference = previousWorkExperiencesReference,
      experiences = listOf(
        aValidWorkExperience(
          experienceType = WorkExperienceTypeDomain.OTHER,
          experienceTypeOther = "Warehouse work",
          role = "Chief Forklift Truck Driver",
          details = "Forward, pick stuff up, reverse etc",
        ),
      ),
      prisonId = "MDI",
    )

    val expectedEntity = existingPreviousWorkExperiencesEntity.copy(
      experiences = mutableListOf(firstWorkExperienceEntity),
      createdAtPrison = "BXI",
      updatedAtPrison = "MDI",
    )

    // When
    mapper.updateExistingEntityFromDto(existingPreviousWorkExperiencesEntity, updatePreviousWorkExperiencesDto)

    // Then
    assertThat(existingPreviousWorkExperiencesEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
  }
}
