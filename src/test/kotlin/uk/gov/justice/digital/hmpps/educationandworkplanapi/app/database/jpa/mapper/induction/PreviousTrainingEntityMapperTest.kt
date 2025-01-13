package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidPreviousTraining
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreatePreviousTrainingDto
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
}
