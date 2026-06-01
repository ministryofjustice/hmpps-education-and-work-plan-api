package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InterestType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.SkillType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidPersonalInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidPersonalSkill
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidPersonalSkillsAndInterests
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPersonalInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPersonalSkillEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPersonalSkillsAndInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import java.time.Instant
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InterestType as InterestTypeEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.SkillType as SkillTypeEntity

class PersonalSkillsAndInterestsEntityMapperTest {

  private val mapper = PersonalSkillsAndInterestsEntityMapper(PersonalSkillEntityMapper(), PersonalInterestEntityMapper())

  @Nested
  inner class FromDtoToEntity {
    @Test
    fun `should map from dto to entity`() {
      // Given
      val createPersonalSkillsAndInterestsDto = aValidCreatePersonalSkillsAndInterestsDto(
        skills = listOf(
          aValidPersonalSkill(
            skillType = SkillType.OTHER,
            skillTypeOther = "Hidden skills",
          ),
        ),
        interests = listOf(
          aValidPersonalInterest(
            interestType = InterestType.OTHER,
            interestTypeOther = "Varied interests",
          ),
        ),
        prisonId = "BXI",
      )

      val expected = aValidPersonalSkillsAndInterestsEntity(
        skills = mutableListOf(
          aValidPersonalSkillEntity(
            skillType = SkillTypeEntity.OTHER,
            skillTypeOther = "Hidden skills",
            // JPA managed fields - expect these all to be null, implying a new db entity
            id = null,
            createdAt = null,
            createdBy = null,
            updatedAt = null,
            updatedBy = null,
          ),
        ),
        interests = mutableListOf(
          aValidPersonalInterestEntity(
            interestType = InterestTypeEntity.OTHER,
            interestTypeOther = "Varied interests",
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
      val actual = mapper.fromCreateDtoToEntity(createPersonalSkillsAndInterestsDto)

      // Then
      assertThat(actual)
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*reference") // Ignore the generated reference field as we cannot predict its value in the expected object
        .ignoringFieldsMatchingRegexes(".*parent") // Ignore the parent field as this is set to establish the bidirectional relationship between PersonalSkillsAndInterestsEntity and the child entities and are not mapped from the source DTO
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
      val personalSkillsAndInterestsId = UUID.randomUUID()
      val personalSkillsAndInterestsReference = UUID.randomUUID()
      val personalSkill1Id = UUID.randomUUID()
      val personalSkill1Reference = UUID.randomUUID()
      val personalInterest1Id = UUID.randomUUID()
      val personalInterest1Reference = UUID.randomUUID()

      val personalSkillsAndInterestsEntity = aValidPersonalSkillsAndInterestsEntity(
        id = personalSkillsAndInterestsId,
        reference = personalSkillsAndInterestsReference,
        skills = mutableListOf(
          aValidPersonalSkillEntity(
            id = personalSkill1Id,
            reference = personalSkill1Reference,
            skillType = SkillTypeEntity.OTHER,
            skillTypeOther = "Hidden skills",
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        interests = mutableListOf(
          aValidPersonalInterestEntity(
            id = personalInterest1Id,
            reference = personalInterest1Reference,
            interestType = InterestTypeEntity.OTHER,
            interestTypeOther = "Varied interests",
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

      val expected = aValidPersonalSkillsAndInterests(
        reference = personalSkillsAndInterestsReference,
        skills = mutableListOf(
          aValidPersonalSkill(
            skillType = SkillType.OTHER,
            skillTypeOther = "Hidden skills",
          ),
        ),
        interests = mutableListOf(
          aValidPersonalInterest(
            interestType = InterestType.OTHER,
            interestTypeOther = "Varied interests",
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
      val actual = mapper.fromEntityToDomain(personalSkillsAndInterestsEntity)

      // Then
      assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    }
  }

  @Nested
  inner class UpdateEntityFromDto {

    @Nested
    inner class PersonalSkills {

      @Test
      fun `should update existing personal skills`() {
        // Given
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val personalSkillsAndInterestsId = UUID.randomUUID()
        val personalSkillsAndInterestsReference = UUID.randomUUID()
        val personalSkill1Id = UUID.randomUUID()
        val personalSkill1Reference = UUID.randomUUID()

        val existingPersonalSkillsAndInterestsEntity = aValidPersonalSkillsAndInterestsEntity(
          id = personalSkillsAndInterestsId,
          reference = personalSkillsAndInterestsReference,
          skills = mutableListOf(
            aValidPersonalSkillEntity(
              id = personalSkill1Id,
              reference = personalSkill1Reference,
              skillType = SkillTypeEntity.OTHER,
              skillTypeOther = "Too many skills to mention",
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
          ),
          interests = mutableListOf(),
          createdAtPrison = "BXI",
          updatedAtPrison = "BXI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        val updatedInterestsDto = aValidUpdatePersonalSkillsAndInterestsDto(
          reference = personalSkillsAndInterestsReference,
          skills = listOf(
            aValidPersonalSkill(
              skillType = SkillType.OTHER,
              skillTypeOther = "Not that many skills actually",
            ),
          ),
          interests = listOf(),
          prisonId = "MDI",
        )

        val expectedEntity = aValidPersonalSkillsAndInterestsEntity(
          id = personalSkillsAndInterestsId,
          reference = personalSkillsAndInterestsReference,
          skills = mutableListOf(
            aValidPersonalSkillEntity(
              id = personalSkill1Id,
              reference = personalSkill1Reference,
              skillType = SkillTypeEntity.OTHER,
              skillTypeOther = "Not that many skills actually",
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
          ),
          interests = mutableListOf(),
          createdAtPrison = "BXI",
          updatedAtPrison = "MDI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        // When
        mapper.updateExistingEntityFromDto(existingPersonalSkillsAndInterestsEntity, updatedInterestsDto)

        // Then
        assertThat(existingPersonalSkillsAndInterestsEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
      }

      @Test
      fun `should add new personal skills`() {
        // Given
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val personalSkillsAndInterestsId = UUID.randomUUID()
        val personalSkillsAndInterestsReference = UUID.randomUUID()
        val personalSkill1Id = UUID.randomUUID()
        val personalSkill1Reference = UUID.randomUUID()

        val existingPersonalSkillsAndInterestsEntity = aValidPersonalSkillsAndInterestsEntity(
          id = personalSkillsAndInterestsId,
          reference = personalSkillsAndInterestsReference,
          skills = mutableListOf(
            aValidPersonalSkillEntity(
              id = personalSkill1Id,
              reference = personalSkill1Reference,
              skillType = SkillTypeEntity.OTHER,
              skillTypeOther = "Too many skills to mention",
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
          ),
          interests = mutableListOf(),
          createdAtPrison = "BXI",
          updatedAtPrison = "BXI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        val updatedInterestsDto = aValidUpdatePersonalSkillsAndInterestsDto(
          reference = personalSkillsAndInterestsReference,
          skills = listOf(
            aValidPersonalSkill(
              skillType = SkillType.OTHER,
              skillTypeOther = "Too many skills to mention",
            ),
            aValidPersonalSkill(
              skillType = SkillType.COMMUNICATION,
              skillTypeOther = null,
            ),
          ),
          interests = listOf(),
          prisonId = "MDI",
        )

        val expectedEntity = aValidPersonalSkillsAndInterestsEntity(
          id = personalSkillsAndInterestsId,
          reference = personalSkillsAndInterestsReference,
          skills = mutableListOf(
            aValidPersonalSkillEntity(
              id = personalSkill1Id,
              reference = personalSkill1Reference,
              skillType = SkillTypeEntity.OTHER,
              skillTypeOther = "Too many skills to mention",
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
            aValidPersonalSkillEntity(
              skillType = SkillTypeEntity.COMMUNICATION,
              skillTypeOther = null,
              // JPA managed fields - expect these all to be null, implying a new db entity
              id = null,
              createdAt = null,
              createdBy = null,
              updatedAt = null,
              updatedBy = null,
            ),
          ),
          interests = mutableListOf(),
          createdAtPrison = "BXI",
          updatedAtPrison = "MDI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        // When
        mapper.updateExistingEntityFromDto(existingPersonalSkillsAndInterestsEntity, updatedInterestsDto)

        // Then
        assertThat(existingPersonalSkillsAndInterestsEntity)
          .usingRecursiveComparison()
          .ignoringFieldsMatchingRegexes(".*parent") // Ignore the parent field as this is set to establish the bidirectional relationship between PersonalSkillsAndInterestsEntity and the child entities and are not mapped from the source DTO
          .ignoringFields("skills.reference") // Ignore the generated reference field for the new personal skill as we cannot predict its value in the expected object
          .isEqualTo(expectedEntity)
      }

      @Test
      fun `should remove personal skills`() {
        // Given
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val personalSkillsAndInterestsId = UUID.randomUUID()
        val personalSkillsAndInterestsReference = UUID.randomUUID()
        val personalSkill1Id = UUID.randomUUID()
        val personalSkill1Reference = UUID.randomUUID()
        val personalSkill2Id = UUID.randomUUID()
        val personalSkill2Reference = UUID.randomUUID()

        val existingPersonalSkillsAndInterestsEntity = aValidPersonalSkillsAndInterestsEntity(
          id = personalSkillsAndInterestsId,
          reference = personalSkillsAndInterestsReference,
          skills = mutableListOf(
            aValidPersonalSkillEntity(
              id = personalSkill1Id,
              reference = personalSkill1Reference,
              skillType = SkillTypeEntity.RESILIENCE,
              skillTypeOther = null,
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
            aValidPersonalSkillEntity(
              id = personalSkill2Id,
              reference = personalSkill2Reference,
              skillType = SkillTypeEntity.COMMUNICATION,
              skillTypeOther = null,
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
          ),
          interests = mutableListOf(),
          createdAtPrison = "BXI",
          updatedAtPrison = "BXI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        val updatedInterestsDto = aValidUpdatePersonalSkillsAndInterestsDto(
          reference = personalSkillsAndInterestsReference,
          skills = listOf(
            aValidPersonalSkill(
              skillType = SkillType.RESILIENCE,
              skillTypeOther = null,
            ),
          ),
          interests = listOf(),
          prisonId = "MDI",
        )

        val expectedEntity = aValidPersonalSkillsAndInterestsEntity(
          id = personalSkillsAndInterestsId,
          reference = personalSkillsAndInterestsReference,
          skills = mutableListOf(
            aValidPersonalSkillEntity(
              id = personalSkill1Id,
              reference = personalSkill1Reference,
              skillType = SkillTypeEntity.RESILIENCE,
              skillTypeOther = null,
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
          ),
          interests = mutableListOf(),
          createdAtPrison = "BXI",
          updatedAtPrison = "MDI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        // When
        mapper.updateExistingEntityFromDto(existingPersonalSkillsAndInterestsEntity, updatedInterestsDto)

        // Then
        assertThat(existingPersonalSkillsAndInterestsEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
      }
    }

    @Nested
    inner class PersonalInterests {
      @Test
      fun `should update existing personal interests`() {
        // Given
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val personalSkillsAndInterestsId = UUID.randomUUID()
        val personalSkillsAndInterestsReference = UUID.randomUUID()
        val personalInterest1Id = UUID.randomUUID()
        val personalInterest1Reference = UUID.randomUUID()

        val existingPersonalSkillsAndInterestsEntity = aValidPersonalSkillsAndInterestsEntity(
          id = personalSkillsAndInterestsId,
          reference = personalSkillsAndInterestsReference,
          skills = mutableListOf(),
          interests = mutableListOf(
            aValidPersonalInterestEntity(
              id = personalInterest1Id,
              reference = personalInterest1Reference,
              interestType = InterestTypeEntity.OTHER,
              interestTypeOther = "Lots of varied interests",
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

        val updatedInterestsDto = aValidUpdatePersonalSkillsAndInterestsDto(
          reference = personalSkillsAndInterestsReference,
          skills = listOf(),
          interests = listOf(
            aValidPersonalInterest(
              interestType = InterestType.OTHER,
              interestTypeOther = "Not such varied interests actually",
            ),
          ),
          prisonId = "MDI",
        )

        val expectedEntity = aValidPersonalSkillsAndInterestsEntity(
          id = personalSkillsAndInterestsId,
          reference = personalSkillsAndInterestsReference,
          skills = mutableListOf(),
          interests = mutableListOf(
            aValidPersonalInterestEntity(
              id = personalInterest1Id,
              reference = personalInterest1Reference,
              interestType = InterestTypeEntity.OTHER,
              interestTypeOther = "Not such varied interests actually",
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
        mapper.updateExistingEntityFromDto(existingPersonalSkillsAndInterestsEntity, updatedInterestsDto)

        // Then
        assertThat(existingPersonalSkillsAndInterestsEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
      }

      @Test
      fun `should add new personal interests`() {
        // Given
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val personalSkillsAndInterestsId = UUID.randomUUID()
        val personalSkillsAndInterestsReference = UUID.randomUUID()
        val personalInterest1Id = UUID.randomUUID()
        val personalInterest1Reference = UUID.randomUUID()

        val existingPersonalSkillsAndInterestsEntity = aValidPersonalSkillsAndInterestsEntity(
          id = personalSkillsAndInterestsId,
          reference = personalSkillsAndInterestsReference,
          skills = mutableListOf(),
          interests = mutableListOf(
            aValidPersonalInterestEntity(
              id = personalInterest1Id,
              reference = personalInterest1Reference,
              interestType = InterestTypeEntity.OTHER,
              interestTypeOther = "Lots of varied interests",
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

        val updatedInterestsDto = aValidUpdatePersonalSkillsAndInterestsDto(
          reference = personalSkillsAndInterestsReference,
          skills = listOf(),
          interests = listOf(
            aValidPersonalInterest(
              interestType = InterestType.OTHER,
              interestTypeOther = "Lots of varied interests",
            ),
            aValidPersonalInterest(
              interestType = InterestType.CRAFTS,
              interestTypeOther = null,
            ),
          ),
          prisonId = "MDI",
        )

        val expectedEntity = aValidPersonalSkillsAndInterestsEntity(
          id = personalSkillsAndInterestsId,
          reference = personalSkillsAndInterestsReference,
          skills = mutableListOf(),
          interests = mutableListOf(
            aValidPersonalInterestEntity(
              id = personalInterest1Id,
              reference = personalInterest1Reference,
              interestType = InterestTypeEntity.OTHER,
              interestTypeOther = "Lots of varied interests",
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
            aValidPersonalInterestEntity(
              interestType = InterestTypeEntity.CRAFTS,
              interestTypeOther = null,
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
        mapper.updateExistingEntityFromDto(existingPersonalSkillsAndInterestsEntity, updatedInterestsDto)

        // Then
        assertThat(existingPersonalSkillsAndInterestsEntity)
          .usingRecursiveComparison()
          .ignoringFieldsMatchingRegexes(".*parent") // Ignore the parent field as this is set to establish the bidirectional relationship between PersonalSkillsAndInterestsEntity and the child entities and are not mapped from the source DTO
          .ignoringFields("interests.reference") // Ignore the generated reference field for the new personal interest as we cannot predict its value in the expected object
          .isEqualTo(expectedEntity)
      }

      @Test
      fun `should remove personal interests`() {
        // Given
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val personalSkillsAndInterestsId = UUID.randomUUID()
        val personalSkillsAndInterestsReference = UUID.randomUUID()
        val personalInterest1Id = UUID.randomUUID()
        val personalInterest1Reference = UUID.randomUUID()
        val inPrisonTrainingInterest2Id = UUID.randomUUID()
        val inPrisonTrainingInterest2Reference = UUID.randomUUID()

        val existingPersonalSkillsAndInterestsEntity = aValidPersonalSkillsAndInterestsEntity(
          id = personalSkillsAndInterestsId,
          reference = personalSkillsAndInterestsReference,
          skills = mutableListOf(),
          interests = mutableListOf(
            aValidPersonalInterestEntity(
              id = personalInterest1Id,
              reference = personalInterest1Reference,
              interestType = InterestTypeEntity.CRAFTS,
              interestTypeOther = null,
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
            aValidPersonalInterestEntity(
              id = inPrisonTrainingInterest2Id,
              reference = inPrisonTrainingInterest2Reference,
              interestType = InterestTypeEntity.NATURE_AND_ANIMALS,
              interestTypeOther = null,
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

        val updatedInterestsDto = aValidUpdatePersonalSkillsAndInterestsDto(
          reference = personalSkillsAndInterestsReference,
          skills = listOf(),
          interests = listOf(
            aValidPersonalInterest(
              interestType = InterestType.CRAFTS,
              interestTypeOther = null,
            ),
          ),
          prisonId = "MDI",
        )

        val expectedEntity = aValidPersonalSkillsAndInterestsEntity(
          id = personalSkillsAndInterestsId,
          reference = personalSkillsAndInterestsReference,
          skills = mutableListOf(),
          interests = mutableListOf(
            aValidPersonalInterestEntity(
              id = personalInterest1Id,
              reference = personalInterest1Reference,
              interestType = InterestTypeEntity.CRAFTS,
              interestTypeOther = null,
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
        mapper.updateExistingEntityFromDto(existingPersonalSkillsAndInterestsEntity, updatedInterestsDto)

        // Then
        assertThat(existingPersonalSkillsAndInterestsEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
      }
    }
  }
}
