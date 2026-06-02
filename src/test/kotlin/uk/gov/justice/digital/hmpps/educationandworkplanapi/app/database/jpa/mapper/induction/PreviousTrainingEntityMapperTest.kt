package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidPreviousTraining
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousTrainingEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousTrainingEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.TrainingType as TrainingTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.TrainingType as TrainingTypeEntity

class PreviousTrainingEntityMapperTest {

  private val mapper = PreviousTrainingEntityMapper()

  @Test
  fun `should map from dto to entity`() {
    // Given
    val createTrainingDto = aValidCreatePreviousTrainingDto()
    val expectedTrainingTypeEntity = TrainingTypeEntity.OTHER
    val expected = aValidPreviousTrainingEntity(
      trainingTypes = mutableListOf(expectedTrainingTypeEntity),
      trainingTypeOther = "Kotlin course",
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )

    // When
    val actual = mapper.fromCreateDtoToEntity(createTrainingDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(".*reference")
      .isEqualTo(expected)
  }

  @Test
  fun `should map from entity to domain`() {
    // Given
    val previousTrainingEntity = aValidPreviousTrainingEntityWithJpaFieldsPopulated()
    val expectedPreviousTraining = aValidPreviousTraining(
      reference = previousTrainingEntity.reference,
      trainingTypes = mutableListOf(TrainingTypeDomain.OTHER),
      trainingTypeOther = "Kotlin course",
      createdAt = previousTrainingEntity.createdAt!!,
      createdAtPrison = previousTrainingEntity.createdAtPrison,
      createdBy = previousTrainingEntity.createdBy!!,
      lastUpdatedAt = previousTrainingEntity.updatedAt!!,
      lastUpdatedAtPrison = previousTrainingEntity.updatedAtPrison,
      lastUpdatedBy = previousTrainingEntity.updatedBy!!,
    )

    // When
    val actual = mapper.fromEntityToDomain(previousTrainingEntity)

    // Then
    assertThat(actual).isEqualTo(expectedPreviousTraining)
  }

  @Nested
  inner class UpdateExistingEntityFromDto {
    @Test
    fun `should update existing entity from DTO given a change to the training types`() {
      // Given
      val existingTrainingTypes = mutableListOf(TrainingTypeEntity.OTHER, TrainingTypeEntity.TRADE_COURSE)
      val entity = aValidPreviousTrainingEntity(
        trainingTypes = existingTrainingTypes,
        trainingTypeOther = "Kotlin course",
        updatedAtPrison = "BXI",
      )

      val updateDto = aValidUpdatePreviousTrainingDto(
        trainingTypes = mutableListOf(TrainingTypeDomain.OTHER),
        trainingTypeOther = "Kotlin course",
        prisonId = "MDI",
      )

      val expectedTrainingTypes = listOf(TrainingTypeEntity.OTHER)

      // When
      mapper.updateExistingEntityFromDto(entity, updateDto)

      // Then
      assertThat(entity.trainingTypes).isEqualTo(expectedTrainingTypes)
      assertThat(entity.trainingTypes).isNotSameAs(existingTrainingTypes) // assert that the training types on the entity have been replaced with a new List instance
    }

    @Test
    fun `should not update existing entity from DTO given no change to the training types`() {
      // Given
      val existingTrainingTypes = mutableListOf(TrainingTypeEntity.OTHER, TrainingTypeEntity.TRADE_COURSE)
      val entity = aValidPreviousTrainingEntity(
        trainingTypes = existingTrainingTypes,
        trainingTypeOther = "Kotlin course",
        updatedAtPrison = "BXI",
      )

      val updateDto = aValidUpdatePreviousTrainingDto(
        trainingTypes = mutableListOf(TrainingTypeDomain.OTHER, TrainingTypeDomain.TRADE_COURSE),
        trainingTypeOther = "Kotlin course",
        prisonId = "MDI",
      )

      // When
      mapper.updateExistingEntityFromDto(entity, updateDto)

      // Then
      // assert that the training types on the entity are the same List instance that they were before - IE. the list itself has not been replaced
      assertThat(entity.trainingTypes).isSameAs(existingTrainingTypes)
    }
  }
}
