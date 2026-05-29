package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InPrisonTrainingType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InPrisonWorkType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInPrisonInterests
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInPrisonWorkInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInPrisonInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInPrisonTrainingInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInPrisonWorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import java.time.Instant
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonTrainingType as InPrisonTrainingTypeEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonWorkType as InPrisonWorkTypeEntity

class InPrisonInterestsEntityMapperTest {

  private val mapper = InPrisonInterestsEntityMapper(
    InPrisonWorkInterestEntityMapper(),
    InPrisonTrainingInterestEntityMapper(),
  )

  @Nested
  inner class FromDtoToEntity {
    @Test
    fun `should map from dto to entity`() {
      // Given
      val createInPrisonInterestsDto = aValidCreateInPrisonInterestsDto(
        inPrisonWorkInterests = listOf(
          aValidInPrisonWorkInterest(
            workType = InPrisonWorkType.OTHER,
            workTypeOther = "Any in-prison work",
          ),
        ),
        inPrisonTrainingInterests = listOf(
          aValidInPrisonTrainingInterest(
            trainingType = InPrisonTrainingType.OTHER,
            trainingTypeOther = "Any in-prison training",
          ),
        ),
        prisonId = "BXI",
      )

      val expected = aValidInPrisonInterestsEntity(
        inPrisonWorkInterests = mutableListOf(
          aValidInPrisonWorkInterestEntity(
            workType = InPrisonWorkTypeEntity.OTHER,
            workTypeOther = "Any in-prison work",
            // JPA managed fields - expect these all to be null, implying a new db entity
            id = null,
            createdAt = null,
            createdBy = null,
            updatedAt = null,
            updatedBy = null,
          ),
        ),
        inPrisonTrainingInterests = mutableListOf(
          aValidInPrisonTrainingInterestEntity(
            trainingType = InPrisonTrainingTypeEntity.OTHER,
            trainingTypeOther = "Any in-prison training",
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
      val actual = mapper.fromCreateDtoToEntity(createInPrisonInterestsDto)

      // Then
      assertThat(actual)
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*reference") // Ignore the generated reference field as we cannot predict its value in the expected object
        .ignoringFieldsMatchingRegexes(".*parent") // Ignore the parent field as this is set to establish the bidirectional relationship between InPrisonInterestsEntity and the child entities and are not mapped from the source DTO
        .isEqualTo(expected)
    }
  }

  @Nested
  inner class FromEntityToDomain {
    @Test
    fun `should map from entity to domain`() {
      // Given
      val createdAt = Instant.now()
      val updatedAt = Instant.now()
      val inPrisonInterestsId = UUID.randomUUID()
      val inPrisonInterestsReference = UUID.randomUUID()
      val inPrisonWorkInterest1Id = UUID.randomUUID()
      val inPrisonWorkInterest1Reference = UUID.randomUUID()
      val inPrisonTrainingInterest1Id = UUID.randomUUID()
      val inPrisonTrainingInterest1Reference = UUID.randomUUID()

      val inPrisonInterestsEntity = aValidInPrisonInterestsEntity(
        id = inPrisonInterestsId,
        reference = inPrisonInterestsReference,
        inPrisonWorkInterests = mutableListOf(
          aValidInPrisonWorkInterestEntity(
            id = inPrisonWorkInterest1Id,
            reference = inPrisonWorkInterest1Reference,
            workType = InPrisonWorkTypeEntity.OTHER,
            workTypeOther = "Any in-prison work",
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        inPrisonTrainingInterests = mutableListOf(
          aValidInPrisonTrainingInterestEntity(
            id = inPrisonTrainingInterest1Id,
            reference = inPrisonTrainingInterest1Reference,
            trainingType = InPrisonTrainingTypeEntity.OTHER,
            trainingTypeOther = "Any in-prison training",
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        createdAtPrison = "BXI",
        updatedAtPrison = "BXI",
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      val expected = aValidInPrisonInterests(
        reference = inPrisonInterestsReference,
        inPrisonWorkInterests = mutableListOf(
          aValidInPrisonWorkInterest(
            workType = InPrisonWorkType.OTHER,
            workTypeOther = "Any in-prison work",
          ),
        ),
        inPrisonTrainingInterests = mutableListOf(
          aValidInPrisonTrainingInterest(
            trainingType = InPrisonTrainingType.OTHER,
            trainingTypeOther = "Any in-prison training",
          ),
        ),
        createdAt = createdAt,
        createdAtPrison = "BXI",
        createdBy = "asmith_gen",
        lastUpdatedAt = updatedAt,
        lastUpdatedAtPrison = "BXI",
        lastUpdatedBy = "bjones_gen",
      )

      // When
      val actual = mapper.fromEntityToDomain(inPrisonInterestsEntity)

      // Then
      assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    }
  }

  @Nested
  inner class UpdateEntityFromDto {

    @Nested
    inner class WorkInterests {

      @Test
      fun `should update existing in prison work interests`() {
        // Given
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val inPrisonInterestsId = UUID.randomUUID()
        val inPrisonInterestsReference = UUID.randomUUID()
        val inPrisonWorkInterest1Id = UUID.randomUUID()
        val inPrisonWorkInterest1Reference = UUID.randomUUID()

        val existingInPrisonInterestsEntity = aValidInPrisonInterestsEntity(
          id = inPrisonInterestsId,
          reference = inPrisonInterestsReference,
          inPrisonWorkInterests = mutableListOf(
            aValidInPrisonWorkInterestEntity(
              id = inPrisonWorkInterest1Id,
              reference = inPrisonWorkInterest1Reference,
              workType = InPrisonWorkTypeEntity.OTHER,
              workTypeOther = "Any in-prison work",
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
          ),
          inPrisonTrainingInterests = mutableListOf(),
          createdAtPrison = "BXI",
          updatedAtPrison = "BXI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        val updatedInterestsDto = aValidUpdateInPrisonInterestsDto(
          reference = inPrisonInterestsReference,
          inPrisonWorkInterests = listOf(
            aValidInPrisonWorkInterest(
              workType = InPrisonWorkType.OTHER,
              workTypeOther = "The in-prison work that pays the most",
            ),
          ),
          inPrisonTrainingInterests = listOf(),
          prisonId = "MDI",
        )

        val expectedEntity = aValidInPrisonInterestsEntity(
          id = inPrisonInterestsId,
          reference = inPrisonInterestsReference,
          inPrisonWorkInterests = mutableListOf(
            aValidInPrisonWorkInterestEntity(
              id = inPrisonWorkInterest1Id,
              reference = inPrisonWorkInterest1Reference,
              workType = InPrisonWorkTypeEntity.OTHER,
              workTypeOther = "The in-prison work that pays the most",
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
          ),
          inPrisonTrainingInterests = mutableListOf(),
          createdAtPrison = "BXI",
          updatedAtPrison = "MDI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        // When
        mapper.updateExistingEntityFromDto(existingInPrisonInterestsEntity, updatedInterestsDto)

        // Then
        assertThat(existingInPrisonInterestsEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
      }

      @Test
      fun `should add new in prison work interests`() {
        // Given
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val inPrisonInterestsId = UUID.randomUUID()
        val inPrisonInterestsReference = UUID.randomUUID()
        val inPrisonWorkInterest1Id = UUID.randomUUID()
        val inPrisonWorkInterest1Reference = UUID.randomUUID()

        val existingInPrisonInterestsEntity = aValidInPrisonInterestsEntity(
          id = inPrisonInterestsId,
          reference = inPrisonInterestsReference,
          inPrisonWorkInterests = mutableListOf(
            aValidInPrisonWorkInterestEntity(
              id = inPrisonWorkInterest1Id,
              reference = inPrisonWorkInterest1Reference,
              workType = InPrisonWorkTypeEntity.OTHER,
              workTypeOther = "Any in-prison work",
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
          ),
          inPrisonTrainingInterests = mutableListOf(),
          createdAtPrison = "BXI",
          updatedAtPrison = "BXI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        val updatedInterestsDto = aValidUpdateInPrisonInterestsDto(
          reference = inPrisonInterestsReference,
          inPrisonWorkInterests = listOf(
            aValidInPrisonWorkInterest(
              workType = InPrisonWorkType.OTHER,
              workTypeOther = "Any in-prison work",
            ),
            aValidInPrisonWorkInterest(
              workType = InPrisonWorkType.COMPUTERS_OR_DESK_BASED,
              workTypeOther = null,
            ),
          ),
          inPrisonTrainingInterests = listOf(),
          prisonId = "MDI",
        )

        val expectedEntity = aValidInPrisonInterestsEntity(
          id = inPrisonInterestsId,
          reference = inPrisonInterestsReference,
          inPrisonWorkInterests = mutableListOf(
            aValidInPrisonWorkInterestEntity(
              id = inPrisonWorkInterest1Id,
              reference = inPrisonWorkInterest1Reference,
              workType = InPrisonWorkTypeEntity.OTHER,
              workTypeOther = "Any in-prison work",
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
            aValidInPrisonWorkInterestEntity(
              workType = InPrisonWorkTypeEntity.COMPUTERS_OR_DESK_BASED,
              workTypeOther = null,
              // JPA managed fields - expect these all to be null, implying a new db entity
              id = null,
              createdAt = null,
              createdBy = null,
              updatedAt = null,
              updatedBy = null,
            ),
          ),
          inPrisonTrainingInterests = mutableListOf(),
          createdAtPrison = "BXI",
          updatedAtPrison = "MDI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        // When
        mapper.updateExistingEntityFromDto(existingInPrisonInterestsEntity, updatedInterestsDto)

        // Then
        assertThat(existingInPrisonInterestsEntity)
          .usingRecursiveComparison()
          .ignoringFieldsMatchingRegexes(".*parent") // Ignore the parent field as this is set to establish the bidirectional relationship between InPrisonInterestsEntity and the child entities and are not mapped from the source DTO
          .ignoringFields("inPrisonWorkInterests.reference") // Ignore the generated reference field for the new work interest as we cannot predict its value in the expected object
          .isEqualTo(expectedEntity)
      }

      @Test
      fun `should remove in prison work interests`() {
        // Given
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val inPrisonInterestsId = UUID.randomUUID()
        val inPrisonInterestsReference = UUID.randomUUID()
        val inPrisonWorkInterest1Id = UUID.randomUUID()
        val inPrisonWorkInterest1Reference = UUID.randomUUID()
        val inPrisonWorkInterest2Id = UUID.randomUUID()
        val inPrisonWorkInterest2Reference = UUID.randomUUID()

        val existingInPrisonInterestsEntity = aValidInPrisonInterestsEntity(
          id = inPrisonInterestsId,
          reference = inPrisonInterestsReference,
          inPrisonWorkInterests = mutableListOf(
            aValidInPrisonWorkInterestEntity(
              id = inPrisonWorkInterest1Id,
              reference = inPrisonWorkInterest1Reference,
              workType = InPrisonWorkTypeEntity.OTHER,
              workTypeOther = "Any in-prison work",
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
            aValidInPrisonWorkInterestEntity(
              id = inPrisonWorkInterest2Id,
              reference = inPrisonWorkInterest2Reference,
              workType = InPrisonWorkTypeEntity.CLEANING_AND_HYGIENE,
              workTypeOther = null,
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
          ),
          inPrisonTrainingInterests = mutableListOf(),
          createdAtPrison = "BXI",
          updatedAtPrison = "BXI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        val updatedInterestsDto = aValidUpdateInPrisonInterestsDto(
          reference = inPrisonInterestsReference,
          inPrisonWorkInterests = listOf(
            aValidInPrisonWorkInterest(
              workType = InPrisonWorkType.OTHER,
              workTypeOther = "Any in-prison work",
            ),
          ),
          inPrisonTrainingInterests = listOf(),
          prisonId = "MDI",
        )

        val expectedEntity = aValidInPrisonInterestsEntity(
          id = inPrisonInterestsId,
          reference = inPrisonInterestsReference,
          inPrisonWorkInterests = mutableListOf(
            aValidInPrisonWorkInterestEntity(
              id = inPrisonWorkInterest1Id,
              reference = inPrisonWorkInterest1Reference,
              workType = InPrisonWorkTypeEntity.OTHER,
              workTypeOther = "Any in-prison work",
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
          ),
          inPrisonTrainingInterests = mutableListOf(),
          createdAtPrison = "BXI",
          updatedAtPrison = "MDI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        // When
        mapper.updateExistingEntityFromDto(existingInPrisonInterestsEntity, updatedInterestsDto)

        // Then
        assertThat(existingInPrisonInterestsEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
      }
    }

    @Nested
    inner class TrainingInterests {
      @Test
      fun `should update existing in prison training interests`() {
        // Given
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val inPrisonInterestsId = UUID.randomUUID()
        val inPrisonInterestsReference = UUID.randomUUID()
        val inPrisonTrainingInterest1Id = UUID.randomUUID()
        val inPrisonTrainingInterest1Reference = UUID.randomUUID()

        val existingInPrisonInterestsEntity = aValidInPrisonInterestsEntity(
          id = inPrisonInterestsId,
          reference = inPrisonInterestsReference,
          inPrisonWorkInterests = mutableListOf(),
          inPrisonTrainingInterests = mutableListOf(
            aValidInPrisonTrainingInterestEntity(
              id = inPrisonTrainingInterest1Id,
              reference = inPrisonTrainingInterest1Reference,
              trainingType = InPrisonTrainingTypeEntity.OTHER,
              trainingTypeOther = "Any in-prison training",
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
          ),
          createdAtPrison = "BXI",
          updatedAtPrison = "BXI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        val updatedInterestsDto = aValidUpdateInPrisonInterestsDto(
          reference = inPrisonInterestsReference,
          inPrisonWorkInterests = listOf(),
          inPrisonTrainingInterests = listOf(
            aValidInPrisonTrainingInterest(
              trainingType = InPrisonTrainingType.OTHER,
              trainingTypeOther = "The in-prison training that will help with my long term goals the most",
            ),
          ),
          prisonId = "MDI",
        )

        val expectedEntity = aValidInPrisonInterestsEntity(
          id = inPrisonInterestsId,
          reference = inPrisonInterestsReference,
          inPrisonWorkInterests = mutableListOf(),
          inPrisonTrainingInterests = mutableListOf(
            aValidInPrisonTrainingInterestEntity(
              id = inPrisonTrainingInterest1Id,
              reference = inPrisonTrainingInterest1Reference,
              trainingType = InPrisonTrainingTypeEntity.OTHER,
              trainingTypeOther = "The in-prison training that will help with my long term goals the most",
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
          ),
          createdAtPrison = "BXI",
          updatedAtPrison = "MDI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        // When
        mapper.updateExistingEntityFromDto(existingInPrisonInterestsEntity, updatedInterestsDto)

        // Then
        assertThat(existingInPrisonInterestsEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
      }

      @Test
      fun `should add new in prison training interests`() {
        // Given
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val inPrisonInterestsId = UUID.randomUUID()
        val inPrisonInterestsReference = UUID.randomUUID()
        val inPrisonTrainingInterest1Id = UUID.randomUUID()
        val inPrisonTrainingInterest1Reference = UUID.randomUUID()

        val existingInPrisonInterestsEntity = aValidInPrisonInterestsEntity(
          id = inPrisonInterestsId,
          reference = inPrisonInterestsReference,
          inPrisonWorkInterests = mutableListOf(),
          inPrisonTrainingInterests = mutableListOf(
            aValidInPrisonTrainingInterestEntity(
              id = inPrisonTrainingInterest1Id,
              reference = inPrisonTrainingInterest1Reference,
              trainingType = InPrisonTrainingTypeEntity.OTHER,
              trainingTypeOther = "Any in-prison training",
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
          ),
          createdAtPrison = "BXI",
          updatedAtPrison = "BXI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        val updatedInterestsDto = aValidUpdateInPrisonInterestsDto(
          reference = inPrisonInterestsReference,
          inPrisonWorkInterests = listOf(),
          inPrisonTrainingInterests = listOf(
            aValidInPrisonTrainingInterest(
              trainingType = InPrisonTrainingType.OTHER,
              trainingTypeOther = "Any in-prison training",
            ),
            aValidInPrisonTrainingInterest(
              trainingType = InPrisonTrainingType.FORKLIFT_DRIVING,
              trainingTypeOther = null,
            ),
          ),
          prisonId = "MDI",
        )

        val expectedEntity = aValidInPrisonInterestsEntity(
          id = inPrisonInterestsId,
          reference = inPrisonInterestsReference,
          inPrisonWorkInterests = mutableListOf(),
          inPrisonTrainingInterests = mutableListOf(
            aValidInPrisonTrainingInterestEntity(
              id = inPrisonTrainingInterest1Id,
              reference = inPrisonTrainingInterest1Reference,
              trainingType = InPrisonTrainingTypeEntity.OTHER,
              trainingTypeOther = "Any in-prison training",
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
            aValidInPrisonTrainingInterestEntity(
              trainingType = InPrisonTrainingTypeEntity.FORKLIFT_DRIVING,
              trainingTypeOther = null,
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
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        // When
        mapper.updateExistingEntityFromDto(existingInPrisonInterestsEntity, updatedInterestsDto)

        // Then
        assertThat(existingInPrisonInterestsEntity)
          .usingRecursiveComparison()
          .ignoringFieldsMatchingRegexes(".*parent") // Ignore the parent field as this is set to establish the bidirectional relationship between InPrisonInterestsEntity and the child entities and are not mapped from the source DTO
          .ignoringFields("inPrisonTrainingInterests.reference") // Ignore the generated reference field for the new training interest as we cannot predict its value in the expected object
          .isEqualTo(expectedEntity)
      }

      @Test
      fun `should remove in prison training interests`() {
        // Given
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val inPrisonInterestsId = UUID.randomUUID()
        val inPrisonInterestsReference = UUID.randomUUID()
        val inPrisonTrainingInterest1Id = UUID.randomUUID()
        val inPrisonTrainingInterest1Reference = UUID.randomUUID()
        val inPrisonTrainingInterest2Id = UUID.randomUUID()
        val inPrisonTrainingInterest2Reference = UUID.randomUUID()

        val existingInPrisonInterestsEntity = aValidInPrisonInterestsEntity(
          id = inPrisonInterestsId,
          reference = inPrisonInterestsReference,
          inPrisonWorkInterests = mutableListOf(),
          inPrisonTrainingInterests = mutableListOf(
            aValidInPrisonTrainingInterestEntity(
              id = inPrisonTrainingInterest1Id,
              reference = inPrisonTrainingInterest1Reference,
              trainingType = InPrisonTrainingTypeEntity.OTHER,
              trainingTypeOther = "Any in-prison training",
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
            aValidInPrisonTrainingInterestEntity(
              id = inPrisonTrainingInterest2Id,
              reference = inPrisonTrainingInterest2Reference,
              trainingType = InPrisonTrainingTypeEntity.CATERING,
              trainingTypeOther = null,
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
          ),
          createdAtPrison = "BXI",
          updatedAtPrison = "BXI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        val updatedInterestsDto = aValidUpdateInPrisonInterestsDto(
          reference = inPrisonInterestsReference,
          inPrisonWorkInterests = listOf(),
          inPrisonTrainingInterests = listOf(
            aValidInPrisonTrainingInterest(
              trainingType = InPrisonTrainingType.OTHER,
              trainingTypeOther = "Any in-prison training",
            ),
          ),
          prisonId = "MDI",
        )

        val expectedEntity = aValidInPrisonInterestsEntity(
          id = inPrisonInterestsId,
          reference = inPrisonInterestsReference,
          inPrisonWorkInterests = mutableListOf(),
          inPrisonTrainingInterests = mutableListOf(
            aValidInPrisonTrainingInterestEntity(
              id = inPrisonTrainingInterest1Id,
              reference = inPrisonTrainingInterest1Reference,
              trainingType = InPrisonTrainingTypeEntity.OTHER,
              trainingTypeOther = "Any in-prison training",
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
          ),
          createdAtPrison = "BXI",
          updatedAtPrison = "MDI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        // When
        mapper.updateExistingEntityFromDto(existingInPrisonInterestsEntity, updatedInterestsDto)

        // Then
        assertThat(existingInPrisonInterestsEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
      }
    }
  }
}
