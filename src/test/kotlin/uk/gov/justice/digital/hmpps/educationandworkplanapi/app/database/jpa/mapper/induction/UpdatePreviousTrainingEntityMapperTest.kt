package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousTrainingEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.deepCopy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidUpdatePreviousTrainingDto
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.TrainingType as TrainingTypeEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.TrainingType as TrainingTypeDomain

class UpdatePreviousTrainingEntityMapperTest {

  private val mapper = PreviousTrainingEntityMapperImpl().also {
    PreviousTrainingEntityMapperImpl::class.java.getDeclaredField("trainingTypeMapper").apply {
      isAccessible = true
      set(it, TrainingTypeMapperImpl())
    }
  }

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

    val expectedEntity = existingPreviousTrainingEntity.deepCopy().apply {
      id
      reference = reference
      trainingTypes = mutableListOf(TrainingTypeEntity.FIRST_AID_CERTIFICATE)
      trainingTypeOther = null
      createdAtPrison = "BXI"
      updatedAtPrison = "MDI"
    }

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

    val expectedEntity = existingPreviousTrainingEntity.deepCopy().apply {
      id
      reference = reference
      trainingTypes = mutableListOf(TrainingTypeEntity.OTHER, TrainingTypeEntity.FIRST_AID_CERTIFICATE)
      trainingTypeOther = "Kotlin course"
      createdAtPrison = "BXI"
      updatedAtPrison = "MDI"
    }

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

    val expectedEntity = existingPreviousTrainingEntity.deepCopy().apply {
      id
      reference = reference
      trainingTypes = mutableListOf(TrainingTypeEntity.FIRST_AID_CERTIFICATE)
      trainingTypeOther = null
      createdAtPrison = "BXI"
      updatedAtPrison = "MDI"
    }

    // When
    mapper.updateExistingEntityFromDto(existingPreviousTrainingEntity, updatedTrainingDto)

    // Then
    assertThat(existingPreviousTrainingEntity).isEqualToComparingAllFields(expectedEntity)
  }
}
