package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.TrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousTrainingEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreatePreviousTrainingDto

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
    val expectedTrainingTypeEntity = TrainingType.OTHER
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
}
