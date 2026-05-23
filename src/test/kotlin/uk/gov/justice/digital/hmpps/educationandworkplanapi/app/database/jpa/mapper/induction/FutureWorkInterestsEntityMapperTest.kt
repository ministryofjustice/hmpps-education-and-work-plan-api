package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkInterestType.OTHER
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkInterestType.SPORTS
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidFutureWorkInterests
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidWorkInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkInterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidFutureWorkInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidWorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import java.time.Instant
import java.util.*

class FutureWorkInterestsEntityMapperTest {

  private val mapper = FutureWorkInterestsEntityMapper(WorkInterestEntityMapper())

  @Nested
  inner class FromDtoToEntity {
    @Test
    fun `should map from dto to entity`() {
      // Given
      val createWorkInterestsDto = aValidCreateFutureWorkInterestsDto(
        interests = listOf(
          aValidWorkInterest(
            workType = OTHER,
            workTypeOther = "Any job I can get",
            role = "Any role",
          ),
        ),
        prisonId = "BXI",
      )

      val expected = aValidFutureWorkInterestsEntity(
        interests = mutableListOf(
          aValidWorkInterestEntity(
            workType = WorkInterestType.OTHER,
            workTypeOther = "Any job I can get",
            role = "Any role",
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
      val actual = mapper.fromCreateDtoToEntity(createWorkInterestsDto)

      // Then
      assertThat(actual)
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*reference") // Ignore the generated reference field as we cannot predict its value in the expected object
        .ignoringFieldsMatchingRegexes(".*parent") // Ignore the parent field as this is set to establish the bidirectional relationship between GoalEntity and StepEntity and is not mapped from the source DTO
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

      val futureWorkInterestsEntity = aValidFutureWorkInterestsEntity(
        id = UUID.randomUUID(),
        reference = UUID.randomUUID(),
        interests = mutableListOf(
          aValidWorkInterestEntity(
            id = UUID.randomUUID(),
            reference = UUID.randomUUID(),
            workType = WorkInterestType.OTHER,
            workTypeOther = "Any job I can get",
            role = "Any role",
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

      val expected = aValidFutureWorkInterests(
        reference = futureWorkInterestsEntity.reference,
        interests = listOf(
          aValidWorkInterest(
            workType = OTHER,
            workTypeOther = "Any job I can get",
            role = "Any role",
          ),
        ),
        createdAt = createdAt,
        createdAtPrison = "BXI",
        createdBy = "asmith_gen",
        lastUpdatedAt = updatedAt,
        lastUpdatedAtPrison = "MDI",
        lastUpdatedBy = "bjones_gen",
      )

      // When
      val actual = mapper.fromEntityToDomain(futureWorkInterestsEntity)

      // Then
      assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    }

    @Nested
    inner class UpdateEntityFromDto {
      @Test
      fun `should update existing work interests`() {
        // Given
        val futureWorkInterestsReference = UUID.randomUUID()
        val futureWorkInterestsId = UUID.randomUUID()
        val workInterestReference = UUID.randomUUID()
        val workInterestId = UUID.randomUUID()
        val createdAt = Instant.now()
        val updatedAt = Instant.now()

        val existingFutureWorkInterestsEntity = aValidFutureWorkInterestsEntity(
          reference = futureWorkInterestsReference,
          interests = mutableListOf(
            aValidWorkInterestEntity(
              reference = workInterestReference,
              workType = WorkInterestType.OTHER,
              workTypeOther = "Any job I can get",
              role = "Any role",
              id = workInterestId,
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
          ),
          id = futureWorkInterestsId,
          createdAtPrison = "BXI",
          updatedAtPrison = "BXI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        val updatedInterestsDto = aValidUpdateFutureWorkInterestsDto(
          reference = futureWorkInterestsReference,
          interests = listOf(
            aValidWorkInterest(
              workType = OTHER,
              workTypeOther = "Any job that pays ok",
              role = null,
            ),
          ),
          prisonId = "MDI",
        )

        val expectedEntity = aValidFutureWorkInterestsEntity(
          reference = futureWorkInterestsReference,
          interests = mutableListOf(
            aValidWorkInterestEntity(
              reference = workInterestReference,
              workType = WorkInterestType.OTHER,
              workTypeOther = "Any job that pays ok",
              role = null,
              id = workInterestId,
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
          ),
          id = futureWorkInterestsId,
          createdAtPrison = "BXI",
          updatedAtPrison = "MDI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        // When
        mapper.updateExistingEntityFromDto(existingFutureWorkInterestsEntity, updatedInterestsDto)

        // Then
        assertThat(existingFutureWorkInterestsEntity).isEqualToComparingAllFields(expectedEntity)
      }

      @Test
      fun `should add new work interest`() {
        // Given
        val futureWorkInterestsReference = UUID.randomUUID()
        val futureWorkInterestsId = UUID.randomUUID()
        val workInterestReference = UUID.randomUUID()
        val workInterestId = UUID.randomUUID()
        val createdAt = Instant.now()
        val updatedAt = Instant.now()

        val existingFutureWorkInterestsEntity = aValidFutureWorkInterestsEntity(
          reference = futureWorkInterestsReference,
          interests = mutableListOf(
            aValidWorkInterestEntity(
              reference = workInterestReference,
              workType = WorkInterestType.OTHER,
              workTypeOther = "Any job I can get",
              role = "Any role",
              id = workInterestId,
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
          ),
          id = futureWorkInterestsId,
          createdAtPrison = "BXI",
          updatedAtPrison = "BXI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        val updatedInterestsDto = aValidUpdateFutureWorkInterestsDto(
          reference = futureWorkInterestsReference,
          interests = listOf(
            aValidWorkInterest(
              workType = OTHER,
              workTypeOther = "Any job that pays ok",
              role = null,
            ),
            aValidWorkInterest(
              workType = SPORTS,
              workTypeOther = null,
              role = "Professional football player",
            ),
          ),
          prisonId = "MDI",
        )

        val expectedEntity = aValidFutureWorkInterestsEntity(
          reference = futureWorkInterestsReference,
          interests = mutableListOf(
            aValidWorkInterestEntity(
              reference = workInterestReference,
              workType = WorkInterestType.OTHER,
              workTypeOther = "Any job that pays ok",
              role = null,
              id = workInterestId,
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
            aValidWorkInterestEntity(
              workType = WorkInterestType.SPORTS,
              workTypeOther = null,
              role = "Professional football player",
              // JPA managed fields - expect these all to be null, implying a new db entity
              id = null,
              createdAt = null,
              createdBy = null,
              updatedAt = null,
              updatedBy = null,
            ),
          ),
          id = futureWorkInterestsId,
          createdAtPrison = "BXI",
          updatedAtPrison = "MDI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        // When
        mapper.updateExistingEntityFromDto(existingFutureWorkInterestsEntity, updatedInterestsDto)

        // Then
        assertThat(existingFutureWorkInterestsEntity)
          .usingRecursiveComparison()
          .ignoringFieldsMatchingRegexes(".*parent") // Ignore the parent field as this is set to establish the bidirectional relationship between FutureWorkInterestsEntity and WorkInterestEntity and is not mapped from the source DTO
          .ignoringFields("interests.reference") // Ignore the generated reference field for the new interest as we cannot predict its value in the expected object
          .isEqualTo(expectedEntity)
      }

      @Test
      fun `should remove work interests`() {
        // Given
        val futureWorkInterestsReference = UUID.randomUUID()
        val futureWorkInterestsId = UUID.randomUUID()
        val workInterest1Reference = UUID.randomUUID()
        val workInterest1Id = UUID.randomUUID()
        val workInterest2Reference = UUID.randomUUID()
        val workInterest2Id = UUID.randomUUID()
        val createdAt = Instant.now()
        val updatedAt = Instant.now()

        val existingFutureWorkInterestsEntity = aValidFutureWorkInterestsEntity(
          reference = futureWorkInterestsReference,
          interests = mutableListOf(
            aValidWorkInterestEntity(
              reference = workInterest1Reference,
              workType = WorkInterestType.OTHER,
              workTypeOther = "Any job I can get",
              role = "Any role",
              id = workInterest1Id,
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
            aValidWorkInterestEntity(
              reference = workInterest2Reference,
              workType = WorkInterestType.CONSTRUCTION,
              workTypeOther = null,
              role = "Bricklaying",
              id = workInterest2Id,
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
          ),
          id = futureWorkInterestsId,
          createdAtPrison = "BXI",
          updatedAtPrison = "BXI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        val updatedInterestsDto = aValidUpdateFutureWorkInterestsDto(
          reference = futureWorkInterestsReference,
          interests = listOf(
            aValidWorkInterest(
              workType = OTHER,
              workTypeOther = "Any job that pays ok",
              role = null,
            ),
          ),
          prisonId = "MDI",
        )

        val expectedEntity = aValidFutureWorkInterestsEntity(
          reference = futureWorkInterestsReference,
          interests = mutableListOf(
            aValidWorkInterestEntity(
              reference = workInterest1Reference,
              workType = WorkInterestType.OTHER,
              workTypeOther = "Any job that pays ok",
              role = null,
              id = workInterest1Id,
              createdAt = createdAt,
              createdBy = "asmith_gen",
              updatedAt = updatedAt,
              updatedBy = "bjones_gen",
            ),
          ),
          id = futureWorkInterestsId,
          createdAtPrison = "BXI",
          updatedAtPrison = "MDI",
          createdAt = createdAt,
          createdBy = "asmith_gen",
          updatedAt = updatedAt,
          updatedBy = "bjones_gen",
        )

        // When
        mapper.updateExistingEntityFromDto(existingFutureWorkInterestsEntity, updatedInterestsDto)

        // Then
        assertThat(existingFutureWorkInterestsEntity).isEqualToComparingAllFields(expectedEntity)
      }
    }
  }
}
