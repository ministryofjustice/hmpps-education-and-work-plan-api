package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HasWorkedBefore
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkExperienceType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidPreviousWorkExperiences
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidWorkExperience
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HasWorkedBefore.YES
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkExperienceType.CONSTRUCTION
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkExperienceType.OTHER
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousWorkExperiencesEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidWorkExperienceEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import java.time.Instant
import java.util.UUID

class PreviousWorkExperiencesEntityMapperTest {

  private val mapper = PreviousWorkExperiencesEntityMapper(WorkExperienceEntityMapper())

  @Nested
  inner class FromDtoToEntity {
    @Test
    fun `should map from dto to entity`() {
      // Given
      val createPreviousWorkExperiencesDto = aValidCreatePreviousWorkExperiencesDto(
        hasWorkedBefore = HasWorkedBefore.YES,
        hasWorkedBeforeNotRelevantReason = null,
        experiences = listOf(
          aValidWorkExperience(
            experienceType = WorkExperienceType.OTHER,
            experienceTypeOther = "All sorts",
            role = "General labourer",
            details = "Helping out where needed",
          ),
        ),
        prisonId = "BXI",
      )

      val expected = aValidPreviousWorkExperiencesEntity(
        hasWorkedBefore = YES,
        hasWorkedBeforeNotRelevantReason = null,
        experiences = mutableListOf(
          aValidWorkExperienceEntity(
            experienceType = OTHER,
            experienceTypeOther = "All sorts",
            role = "General labourer",
            details = "Helping out where needed",
            // JPA managed fields - expect these all to be null, implying a new db entity
            id = null,
            createdAt = null,
            createdBy = null,
            updatedAt = null,
            updatedBy = null,
          ),
        ),
        createdAtPrison = "BXI",
        updatedAtPrison = "BXI",
        // JPA managed fields - expect these all to be null, implying a new db entity
        id = null,
        createdAt = null,
        createdBy = null,
        updatedAt = null,
        updatedBy = null,
      )

      // When
      val actual = mapper.fromCreateDtoToEntity(createPreviousWorkExperiencesDto)

      // Then
      assertThat(actual)
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*reference") // Ignore the generated reference field as we cannot predict its value in the expected object
        .ignoringFieldsMatchingRegexes(".*parent") // Ignore the parent field as this is set to establish the bidirectional relationship between PreviousWorkExperiencesEntity and WorkExperienceEntity and is not mapped from the source DTO
        .isEqualTo(expected)
    }
  }

  @Nested
  inner class FromEntityToDomain {
    @Test
    fun `should map from entity to domain`() {
      // Given
      val previousWorkExperiencesReference = UUID.randomUUID()
      val previousWorkExperiencesId = UUID.randomUUID()
      val workExperienceReference = UUID.randomUUID()
      val workExperienceId = UUID.randomUUID()
      val createdAt = Instant.now()
      val updatedAt = Instant.now()

      val previousWorkExperiencesEntity = aValidPreviousWorkExperiencesEntity(
        reference = previousWorkExperiencesReference,
        experiences = mutableListOf(
          aValidWorkExperienceEntity(
            reference = workExperienceReference,
            experienceType = OTHER,
            experienceTypeOther = "Warehouse work",
            role = "Chief Forklift Truck Driver",
            details = "Forward, pick stuff up, reverse etc",
            id = workExperienceId,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        createdAtPrison = "BXI",
        updatedAtPrison = "BXI",
        id = previousWorkExperiencesId,
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      val expected = aValidPreviousWorkExperiences(
        reference = previousWorkExperiencesReference,
        experiences = mutableListOf(
          aValidWorkExperience(
            experienceType = WorkExperienceType.OTHER,
            experienceTypeOther = "Warehouse work",
            role = "Chief Forklift Truck Driver",
            details = "Forward, pick stuff up, reverse etc",
          ),
        ),
        createdAtPrison = "BXI",
        lastUpdatedAtPrison = "BXI",
        createdAt = createdAt,
        createdBy = "asmith_gen",
        lastUpdatedAt = updatedAt,
        lastUpdatedBy = "bjones_gen",
      )

      // When
      val actual = mapper.fromEntityToDomain(previousWorkExperiencesEntity)

      // Then
      assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    }
  }

  @Nested
  inner class UpdateEntityFromDto {
    @Test
    fun `should update existing work experiences`() {
      // Given
      val previousWorkExperiencesReference = UUID.randomUUID()
      val previousWorkExperiencesId = UUID.randomUUID()
      val workExperienceReference = UUID.randomUUID()
      val workExperienceId = UUID.randomUUID()
      val createdAt = Instant.now()
      val updatedAt = Instant.now()

      val existingPreviousWorkExperiencesEntity = aValidPreviousWorkExperiencesEntity(
        reference = previousWorkExperiencesReference,
        experiences = mutableListOf(
          aValidWorkExperienceEntity(
            reference = workExperienceReference,
            experienceType = OTHER,
            experienceTypeOther = "Warehouse work",
            role = "Chief Forklift Truck Driver",
            details = "Forward, pick stuff up, reverse etc",
            id = workExperienceId,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        createdAtPrison = "BXI",
        updatedAtPrison = "BXI",
        id = previousWorkExperiencesId,
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      val updatedExperiencesDto = aValidUpdatePreviousWorkExperiencesDto(
        reference = previousWorkExperiencesReference,
        experiences = listOf(
          aValidWorkExperience(
            experienceType = WorkExperienceType.OTHER,
            experienceTypeOther = "Building work",
            role = "Bricklaying",
            details = "Various types of bricklaying",
          ),
        ),
        prisonId = "MDI",
      )

      val expectedEntity = aValidPreviousWorkExperiencesEntity(
        reference = previousWorkExperiencesReference,
        experiences = mutableListOf(
          aValidWorkExperienceEntity(
            reference = workExperienceReference,
            experienceType = OTHER,
            experienceTypeOther = "Building work",
            role = "Bricklaying",
            details = "Various types of bricklaying",
            id = workExperienceId,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        createdAtPrison = "BXI",
        updatedAtPrison = "MDI",
        id = previousWorkExperiencesId,
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      // When
      mapper.updateExistingEntityFromDto(existingPreviousWorkExperiencesEntity, updatedExperiencesDto)

      // Then
      assertThat(existingPreviousWorkExperiencesEntity).isEqualToComparingAllFields(expectedEntity)
    }

    @Test
    fun `should not update existing work experiences given no changes`() {
      // Given
      val previousWorkExperiencesReference = UUID.randomUUID()
      val previousWorkExperiencesId = UUID.randomUUID()
      val workExperienceReference = UUID.randomUUID()
      val workExperienceId = UUID.randomUUID()
      val createdAt = Instant.now()
      val updatedAt = Instant.now()

      val existingPreviousWorkExperiencesEntity = aValidPreviousWorkExperiencesEntity(
        reference = previousWorkExperiencesReference,
        experiences = mutableListOf(
          aValidWorkExperienceEntity(
            reference = workExperienceReference,
            experienceType = OTHER,
            experienceTypeOther = "Warehouse work",
            role = "Chief Forklift Truck Driver",
            details = "Forward, pick stuff up, reverse etc",
            id = workExperienceId,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        createdAtPrison = "BXI",
        updatedAtPrison = "BXI",
        id = previousWorkExperiencesId,
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      val updatedExperiencesDto = aValidUpdatePreviousWorkExperiencesDto(
        reference = previousWorkExperiencesReference,
        experiences = listOf(
          aValidWorkExperience(
            experienceType = WorkExperienceType.OTHER,
            experienceTypeOther = "Warehouse work",
            role = "Chief Forklift Truck Driver",
            details = "Forward, pick stuff up, reverse etc",
          ),
        ),
        prisonId = "MDI",
      )

      val expectedEntity = aValidPreviousWorkExperiencesEntity(
        reference = previousWorkExperiencesReference,
        experiences = mutableListOf(
          aValidWorkExperienceEntity(
            reference = workExperienceReference,
            experienceType = OTHER,
            experienceTypeOther = "Warehouse work",
            role = "Chief Forklift Truck Driver",
            details = "Forward, pick stuff up, reverse etc",
            id = workExperienceId,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        createdAtPrison = "BXI",
        updatedAtPrison = "MDI",
        id = previousWorkExperiencesId,
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      // When
      mapper.updateExistingEntityFromDto(existingPreviousWorkExperiencesEntity, updatedExperiencesDto)

      // Then
      assertThat(existingPreviousWorkExperiencesEntity).isEqualToComparingAllFields(expectedEntity)
    }

    @Test
    fun `should add new work experiences`() {
      // Given
      val previousWorkExperiencesReference = UUID.randomUUID()
      val previousWorkExperiencesId = UUID.randomUUID()
      val workExperienceReference = UUID.randomUUID()
      val workExperienceId = UUID.randomUUID()
      val createdAt = Instant.now()
      val updatedAt = Instant.now()

      val existingPreviousWorkExperiencesEntity = aValidPreviousWorkExperiencesEntity(
        reference = previousWorkExperiencesReference,
        experiences = mutableListOf(
          aValidWorkExperienceEntity(
            reference = workExperienceReference,
            experienceType = OTHER,
            experienceTypeOther = "Warehouse work",
            role = "Chief Forklift Truck Driver",
            details = "Forward, pick stuff up, reverse etc",
            id = workExperienceId,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        createdAtPrison = "BXI",
        updatedAtPrison = "BXI",
        id = previousWorkExperiencesId,
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      val updatedExperiencesDto = aValidUpdatePreviousWorkExperiencesDto(
        reference = previousWorkExperiencesReference,
        experiences = listOf(
          aValidWorkExperience(
            experienceType = WorkExperienceType.OTHER,
            experienceTypeOther = "Warehouse work",
            role = "Chief Forklift Truck Driver",
            details = "Forward, pick stuff up, reverse etc",
          ),
          aValidWorkExperience(
            experienceType = WorkExperienceType.CONSTRUCTION,
            experienceTypeOther = null,
            role = "Bricklaying",
            details = "Various types of bricklaying",
          ),
        ),
        prisonId = "MDI",
      )

      val expectedEntity = aValidPreviousWorkExperiencesEntity(
        reference = previousWorkExperiencesReference,
        experiences = mutableListOf(
          aValidWorkExperienceEntity(
            reference = workExperienceReference,
            experienceType = OTHER,
            experienceTypeOther = "Warehouse work",
            role = "Chief Forklift Truck Driver",
            details = "Forward, pick stuff up, reverse etc",
            id = workExperienceId,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
          aValidWorkExperienceEntity(
            experienceType = CONSTRUCTION,
            experienceTypeOther = null,
            role = "Bricklaying",
            details = "Various types of bricklaying",
            // JPA managed fields - expect these all to be null, implying a new db entity
            id = null,
            createdAt = null,
            createdBy = null,
            updatedAt = null,
            updatedBy = null,
          ),
        ),
        createdAtPrison = "BXI",
        updatedAtPrison = "MDI",
        id = previousWorkExperiencesId,
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      // When
      mapper.updateExistingEntityFromDto(existingPreviousWorkExperiencesEntity, updatedExperiencesDto)

      // Then
      assertThat(existingPreviousWorkExperiencesEntity)
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*parent") // Ignore the parent field as this is set to establish the bidirectional relationship between PreviousWorkExperiencesEntity and WorkExperienceEntity and is not mapped from the source DTO
        .ignoringFields("experiences.reference") // Ignore the generated reference field for the new work experience as we cannot predict its value in the expected object
        .isEqualTo(expectedEntity)
    }

    @Test
    fun `should remove work experiences`() {
      // Given
      val previousWorkExperiencesReference = UUID.randomUUID()
      val previousWorkExperiencesId = UUID.randomUUID()
      val workExperience1Reference = UUID.randomUUID()
      val workExperience1Id = UUID.randomUUID()
      val workExperience2Reference = UUID.randomUUID()
      val workExperience2Id = UUID.randomUUID()
      val createdAt = Instant.now()
      val updatedAt = Instant.now()

      val existingPreviousWorkExperiencesEntity = aValidPreviousWorkExperiencesEntity(
        reference = previousWorkExperiencesReference,
        experiences = mutableListOf(
          aValidWorkExperienceEntity(
            reference = workExperience1Reference,
            experienceType = OTHER,
            experienceTypeOther = "Warehouse work",
            role = "Chief Forklift Truck Driver",
            details = "Forward, pick stuff up, reverse etc",
            id = workExperience1Id,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
          aValidWorkExperienceEntity(
            reference = workExperience2Reference,
            experienceType = CONSTRUCTION,
            experienceTypeOther = null,
            role = "Bricklaying",
            details = "Various types of bricklaying",
            id = workExperience2Id,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        createdAtPrison = "BXI",
        updatedAtPrison = "BXI",
        id = previousWorkExperiencesId,
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      val updatedExperiencesDto = aValidUpdatePreviousWorkExperiencesDto(
        reference = previousWorkExperiencesReference,
        experiences = listOf(
          aValidWorkExperience(
            experienceType = WorkExperienceType.OTHER,
            experienceTypeOther = "Warehouse work",
            role = "Chief Forklift Truck Driver",
            details = "Forward, pick stuff up, reverse etc",
          ),
        ),
        prisonId = "MDI",
      )

      val expectedEntity = aValidPreviousWorkExperiencesEntity(
        reference = previousWorkExperiencesReference,
        experiences = mutableListOf(
          aValidWorkExperienceEntity(
            reference = workExperience1Reference,
            experienceType = OTHER,
            experienceTypeOther = "Warehouse work",
            role = "Chief Forklift Truck Driver",
            details = "Forward, pick stuff up, reverse etc",
            id = workExperience1Id,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        createdAtPrison = "BXI",
        updatedAtPrison = "MDI",
        id = previousWorkExperiencesId,
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      // When
      mapper.updateExistingEntityFromDto(existingPreviousWorkExperiencesEntity, updatedExperiencesDto)

      // Then
      assertThat(existingPreviousWorkExperiencesEntity).isEqualToComparingAllFields(expectedEntity)
    }
  }
}
