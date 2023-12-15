package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkExperienceEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousWorkExperiencesEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidWorkExperienceEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.deepCopy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkExperience
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidWorkExperience
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidUpdatePreviousWorkExperiencesDto
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkExperienceType as WorkExperienceTypeEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkExperienceType as WorkExperienceTypeDomain

class UpdatePreviousWorkExperiencesEntityMapperTest {

  private val mapper = PreviousWorkExperiencesEntityMapperImpl().also {
    PreviousWorkExperiencesEntityMapper::class.java.getDeclaredField("workExperienceEntityMapper").apply {
      isAccessible = true
      set(it, WorkExperienceEntityMapperImpl())
    }

    PreviousWorkExperiencesEntityMapper::class.java.getDeclaredField("entityListManager").apply {
      isAccessible = true
      set(it, InductionEntityListManager<WorkExperienceEntity, WorkExperience>())
    }
  }

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

    val expectedEntity = existingPreviousWorkExperiencesEntity.deepCopy().apply {
      id
      reference = reference
      experiences = mutableListOf(
        existingEntity.deepCopy().apply {
          experienceType = WorkExperienceTypeEntity.OTHER
          experienceTypeOther = "Building work"
          role = "Bricklaying"
          details = "Various types of bricklaying"
        },
      )
      createdAtPrison = "BXI"
      updatedAtPrison = "MDI"
    }

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

    val expectedEntity = existingPreviousWorkExperiencesEntity.deepCopy().apply {
      id
      reference = reference
    }

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

    val expectedEntity = existingPreviousWorkExperiencesEntity.deepCopy().apply {
      id
      reference = reference
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
      )
      createdAtPrison = "BXI"
      updatedAtPrison = "MDI"
    }

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

    val expectedEntity = existingPreviousWorkExperiencesEntity.deepCopy().apply {
      id
      reference = reference
      experiences = mutableListOf(firstWorkExperienceEntity)
      createdAtPrison = "BXI"
      updatedAtPrison = "MDI"
    }

    // When
    mapper.updateExistingEntityFromDto(existingPreviousWorkExperiencesEntity, updatePreviousWorkExperiencesDto)

    // Then
    assertThat(existingPreviousWorkExperiencesEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
  }
}
