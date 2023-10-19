package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousTrainingEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousTrainingEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidPreviousTraining
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.TrainingType as TrainingTypeEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.TrainingType as TrainingTypeDomain

@ExtendWith(MockitoExtension::class)
class PreviousTrainingEntityMapperTest {
  @InjectMocks
  private lateinit var mapper: PreviousTrainingEntityMapperImpl

  @Mock
  private lateinit var trainingTypeMapper: TrainingTypeMapper

  @Test
  fun `should map from dto to entity`() {
    // Given
    val createTrainingDto = aValidCreatePreviousTrainingDto()
    val expectedTrainingTypeEntity = TrainingTypeEntity.OTHER
    val expected = aValidPreviousTrainingEntity(
      trainingTypes = listOf(expectedTrainingTypeEntity),
      trainingTypeOther = "Kotlin course",
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )
    given(trainingTypeMapper.fromDomainToEntity(any())).willReturn(listOf(expectedTrainingTypeEntity))

    // When
    val actual = mapper.fromDtoToEntity(createTrainingDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .hasAReference()
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(".*reference")
      .isEqualTo(expected)
    verify(trainingTypeMapper).fromDomainToEntity(createTrainingDto.trainingTypes)
  }

  @Test
  fun `should map from entity to domain`() {
    // Given
    val previousTrainingEntity = aValidPreviousTrainingEntityWithJpaFieldsPopulated()
    val expectedPreviousTraining = aValidPreviousTraining(
      reference = previousTrainingEntity.reference!!,
      trainingTypes = listOf(TrainingTypeDomain.OTHER),
      trainingTypeOther = "Kotlin course",
      createdAt = previousTrainingEntity.createdAt!!,
      createdAtPrison = previousTrainingEntity.createdAtPrison!!,
      createdBy = previousTrainingEntity.createdBy!!,
      createdByDisplayName = previousTrainingEntity.createdByDisplayName!!,
      lastUpdatedAt = previousTrainingEntity.updatedAt!!,
      lastUpdatedAtPrison = previousTrainingEntity.updatedAtPrison!!,
      lastUpdatedBy = previousTrainingEntity.updatedBy!!,
      lastUpdatedByDisplayName = previousTrainingEntity.updatedByDisplayName!!,
    )
    given(trainingTypeMapper.fromEntityToDomain(any())).willReturn(listOf(TrainingTypeDomain.OTHER))

    // When
    val actual = mapper.fromEntityToDomain(previousTrainingEntity)

    // Then
    assertThat(actual).isEqualTo(expectedPreviousTraining)
    verify(trainingTypeMapper).fromEntityToDomain(previousTrainingEntity.trainingTypes!!)
  }
}
