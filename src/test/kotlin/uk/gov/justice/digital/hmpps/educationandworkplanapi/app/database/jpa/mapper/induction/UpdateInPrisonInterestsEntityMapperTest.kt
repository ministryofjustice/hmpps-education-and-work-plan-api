package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInPrisonWorkInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInPrisonInterestsEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInPrisonTrainingInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInPrisonWorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InPrisonTrainingType as InPrisonTrainingTypeDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InPrisonWorkType as InPrisonWorkTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonTrainingType as InPrisonTrainingTypeEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonWorkType as InPrisonWorkTypeEntity

class UpdateInPrisonInterestsEntityMapperTest {

  private val mapper = InPrisonInterestsEntityMapper(
    InPrisonWorkInterestEntityMapper(),
    InPrisonTrainingInterestEntityMapper(),
    InductionEntityListManager(),
    InductionEntityListManager(),
  )

  @Test
  fun `should update existing interests`() {
    // Given
    val workInterestReference = UUID.randomUUID()
    val existingWorkInterestEntity = aValidInPrisonWorkInterestEntity(
      reference = workInterestReference,
      workType = InPrisonWorkTypeEntity.OTHER,
      workTypeOther = "Any job I can get",
    )
    val trainingInterestReference = UUID.randomUUID()
    val existingTrainingInterestEntity = aValidInPrisonTrainingInterestEntity(
      reference = trainingInterestReference,
      trainingType = InPrisonTrainingTypeEntity.OTHER,
      trainingTypeOther = "Any training I can get",
    )
    val inPrisonInterestsReference = UUID.randomUUID()
    val existingInPrisonInterestsEntity = aValidInPrisonInterestsEntityWithJpaFieldsPopulated(
      reference = inPrisonInterestsReference,
      inPrisonWorkInterests = mutableListOf(existingWorkInterestEntity),
      inPrisonTrainingInterests = mutableListOf(existingTrainingInterestEntity),
    )
    // val initialUpdatedAt = existingInPrisonInterestsEntity.updatedAt!!

    val updatedWorkInterest = aValidInPrisonWorkInterest(
      workType = InPrisonWorkTypeDomain.OTHER,
      workTypeOther = "The most popular work",
    )
    val updatedTrainingInterest = aValidInPrisonTrainingInterest(
      trainingType = InPrisonTrainingTypeDomain.OTHER,
      trainingTypeOther = "The most popular training",
    )
    val updatedInterestsDto = aValidUpdateInPrisonInterestsDto(
      reference = inPrisonInterestsReference,
      inPrisonWorkInterests = listOf(updatedWorkInterest),
      inPrisonTrainingInterests = listOf(updatedTrainingInterest),
      prisonId = "MDI",
    )

    val expectedEntity = existingInPrisonInterestsEntity.copy(
      inPrisonWorkInterests = mutableListOf(
        existingWorkInterestEntity.copy(
          workType = InPrisonWorkTypeEntity.OTHER,
          workTypeOther = "The most popular work",
        ),
      ),
      inPrisonTrainingInterests = mutableListOf(
        existingTrainingInterestEntity.copy(
          trainingType = InPrisonTrainingTypeEntity.OTHER,
          trainingTypeOther = "The most popular training",
        ),
      ),
      createdAtPrison = "BXI",
      updatedAtPrison = "MDI",
    )

    // When
    mapper.updateExistingEntityFromDto(existingInPrisonInterestsEntity, updatedInterestsDto)

    // Then
    assertThat(existingInPrisonInterestsEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
  }

  @Test
  fun `should add new interests`() {
    // Given
    val workInterestReference = UUID.randomUUID()
    val existingWorkInterestEntity = aValidInPrisonWorkInterestEntity(
      reference = workInterestReference,
      workType = InPrisonWorkTypeEntity.OTHER,
      workTypeOther = "Any job I can get",
    )
    val trainingInterestReference = UUID.randomUUID()
    val existingTrainingInterestEntity = aValidInPrisonTrainingInterestEntity(
      reference = trainingInterestReference,
      trainingType = InPrisonTrainingTypeEntity.OTHER,
      trainingTypeOther = "Any training I can get",
    )
    val inPrisonInterestsReference = UUID.randomUUID()
    val existingInPrisonInterestsEntity = aValidInPrisonInterestsEntityWithJpaFieldsPopulated(
      reference = inPrisonInterestsReference,
      inPrisonWorkInterests = mutableListOf(existingWorkInterestEntity),
      inPrisonTrainingInterests = mutableListOf(existingTrainingInterestEntity),
    )

    val existingWorkInterest = aValidInPrisonWorkInterest(
      workType = InPrisonWorkTypeDomain.OTHER,
      workTypeOther = "Any job I can get",
    )
    val existingTrainingInterest = aValidInPrisonTrainingInterest(
      trainingType = InPrisonTrainingTypeDomain.OTHER,
      trainingTypeOther = "Any training I can get",
    )
    val newWorkInterest = aValidInPrisonWorkInterest(
      workType = InPrisonWorkTypeDomain.COMPUTERS_OR_DESK_BASED,
      workTypeOther = null,
    )
    val newTrainingInterest = aValidInPrisonTrainingInterest(
      trainingType = InPrisonTrainingTypeDomain.NUMERACY_SKILLS,
      trainingTypeOther = null,
    )
    val updatedInterestsDto = aValidUpdateInPrisonInterestsDto(
      reference = inPrisonInterestsReference,
      inPrisonWorkInterests = listOf(existingWorkInterest, newWorkInterest),
      inPrisonTrainingInterests = listOf(existingTrainingInterest, newTrainingInterest),
      prisonId = "MDI",
    )

    val expectedEntity = existingInPrisonInterestsEntity.copy(
      inPrisonWorkInterests = mutableListOf(
        aValidInPrisonWorkInterestEntity(
          workType = InPrisonWorkTypeEntity.OTHER,
          workTypeOther = "Any job I can get",
        ),
        aValidInPrisonWorkInterestEntity(
          workType = InPrisonWorkTypeEntity.COMPUTERS_OR_DESK_BASED,
          workTypeOther = null,
        ),
      ),
      inPrisonTrainingInterests = mutableListOf(
        aValidInPrisonTrainingInterestEntity(
          trainingType = InPrisonTrainingTypeEntity.OTHER,
          trainingTypeOther = "Any training I can get",
        ),
        aValidInPrisonTrainingInterestEntity(
          trainingType = InPrisonTrainingTypeEntity.NUMERACY_SKILLS,
          trainingTypeOther = null,
        ),
      ),
      createdAtPrison = "BXI",
      updatedAtPrison = "MDI",
    )

    // When
    mapper.updateExistingEntityFromDto(existingInPrisonInterestsEntity, updatedInterestsDto)

    // Then
    assertThat(existingInPrisonInterestsEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
  }

  @Test
  fun `should remove interests`() {
    // Given
    val workInterestReference = UUID.randomUUID()
    val firstWorkInterestEntity = aValidInPrisonWorkInterestEntity(
      reference = workInterestReference,
      workType = InPrisonWorkTypeEntity.OTHER,
      workTypeOther = "Any job I can get",
    )
    val secondWorkInterestEntity = aValidInPrisonWorkInterestEntity(
      workType = InPrisonWorkTypeEntity.CLEANING_AND_HYGIENE,
      workTypeOther = null,
    )
    val trainingInterestReference = UUID.randomUUID()
    val firstTrainingInterestEntity = aValidInPrisonTrainingInterestEntity(
      reference = trainingInterestReference,
      trainingType = InPrisonTrainingTypeEntity.OTHER,
      trainingTypeOther = "Any training I can get",
    )
    val secondTrainingInterestEntity = aValidInPrisonTrainingInterestEntity(
      trainingType = InPrisonTrainingTypeEntity.CATERING,
      trainingTypeOther = null,
    )
    val inPrisonInterestsReference = UUID.randomUUID()
    val existingInPrisonInterestsEntity = aValidInPrisonInterestsEntityWithJpaFieldsPopulated(
      reference = inPrisonInterestsReference,
      inPrisonWorkInterests = mutableListOf(firstWorkInterestEntity, secondWorkInterestEntity),
      inPrisonTrainingInterests = mutableListOf(firstTrainingInterestEntity, secondTrainingInterestEntity),
    )

    val existingWorkInterest = aValidInPrisonWorkInterest(
      workType = InPrisonWorkTypeDomain.OTHER,
      workTypeOther = "Any job I can get",
    )
    val existingTrainingInterest = aValidInPrisonTrainingInterest(
      trainingType = InPrisonTrainingTypeDomain.OTHER,
      trainingTypeOther = "Any training I can get",
    )
    val updatedInterestsDto = aValidUpdateInPrisonInterestsDto(
      reference = inPrisonInterestsReference,
      inPrisonWorkInterests = listOf(existingWorkInterest),
      inPrisonTrainingInterests = listOf(existingTrainingInterest),
      prisonId = "MDI",
    )

    val expectedEntity = existingInPrisonInterestsEntity.copy(
      inPrisonWorkInterests = mutableListOf(firstWorkInterestEntity),
      inPrisonTrainingInterests = mutableListOf(firstTrainingInterestEntity),
      createdAtPrison = "BXI",
      updatedAtPrison = "MDI",
    )

    // When
    mapper.updateExistingEntityFromDto(existingInPrisonInterestsEntity, updatedInterestsDto)

    // Then
    assertThat(existingInPrisonInterestsEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
  }
}
