package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousTrainingEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.TrainingType as TrainingTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.TrainingType as TrainingTypeEntity

class UpdatePreviousTrainingEntityMapperTest {

  private val mapper = PreviousTrainingEntityMapper()

  @Test
  fun `should update existing training`() {
    // Given
    val futureWorkInterestsReference = UUID.randomUUID()
    val existingPreviousTrainingEntity = aValidPreviousTrainingEntity(
      reference = futureWorkInterestsReference,
      trainingTypes = mutableListOf(TrainingTypeEntity.OTHER),
      trainingTypeOther = "Kotlin course",
    )

    val updatedTrainingDto = aValidUpdatePreviousTrainingDto(
      reference = futureWorkInterestsReference,
      trainingTypes = mutableListOf(TrainingTypeDomain.FIRST_AID_CERTIFICATE),
      trainingTypeOther = null,
      prisonId = "MDI",
    )

    val expectedEntity = existingPreviousTrainingEntity.copy(
      trainingTypes = mutableListOf(TrainingTypeEntity.FIRST_AID_CERTIFICATE),
      trainingTypeOther = null,
      createdAtPrison = "BXI",
      updatedAtPrison = "MDI",
    )

    // When
    mapper.updateExistingEntityFromDto(existingPreviousTrainingEntity, updatedTrainingDto)

    // Then
    assertThat(existingPreviousTrainingEntity).isEqualToComparingAllFields(expectedEntity)
  }

  @Test
  fun `should add new training`() {
    // Given
    val futureWorkInterestsReference = UUID.randomUUID()
    val existingPreviousTrainingEntity = aValidPreviousTrainingEntity(
      reference = futureWorkInterestsReference,
      trainingTypes = mutableListOf(TrainingTypeEntity.OTHER),
      trainingTypeOther = "Kotlin course",
    )
    val updatedTrainingDto = aValidUpdatePreviousTrainingDto(
      reference = futureWorkInterestsReference,
      trainingTypes = mutableListOf(TrainingTypeDomain.OTHER, TrainingTypeDomain.FIRST_AID_CERTIFICATE),
      trainingTypeOther = "Kotlin course",
      prisonId = "MDI",
    )

    val expectedEntity = existingPreviousTrainingEntity.copy(
      trainingTypes = mutableListOf(TrainingTypeEntity.OTHER, TrainingTypeEntity.FIRST_AID_CERTIFICATE),
      trainingTypeOther = "Kotlin course",
      createdAtPrison = "BXI",
      updatedAtPrison = "MDI",
    )

    // When
    mapper.updateExistingEntityFromDto(existingPreviousTrainingEntity, updatedTrainingDto)

    // Then
    assertThat(existingPreviousTrainingEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
  }

  @Test
  fun `should remove training`() {
    // Given
    val futureWorkInterestsReference = UUID.randomUUID()
    val existingPreviousTrainingEntity = aValidPreviousTrainingEntity(
      reference = futureWorkInterestsReference,
      trainingTypes = mutableListOf(TrainingTypeEntity.OTHER, TrainingTypeEntity.FIRST_AID_CERTIFICATE),
      trainingTypeOther = "Kotlin course",
    )
    val updatedTrainingDto = aValidUpdatePreviousTrainingDto(
      reference = futureWorkInterestsReference,
      trainingTypes = mutableListOf(TrainingTypeDomain.FIRST_AID_CERTIFICATE),
      trainingTypeOther = null,
      prisonId = "MDI",
    )

    val expectedEntity = existingPreviousTrainingEntity.copy(
      trainingTypes = mutableListOf(TrainingTypeEntity.FIRST_AID_CERTIFICATE),
      trainingTypeOther = null,
      createdAtPrison = "BXI",
      updatedAtPrison = "MDI",
    )

    // When
    mapper.updateExistingEntityFromDto(existingPreviousTrainingEntity, updatedTrainingDto)

    // Then
    assertThat(existingPreviousTrainingEntity).isEqualToComparingAllFields(expectedEntity)
  }
}
