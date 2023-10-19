package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousWorkExperiencesEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidWorkExperienceEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreatePreviousWorkExperiencesDto

@ExtendWith(MockitoExtension::class)
class PreviousWorkExperiencesEntityMapperTest {
  @InjectMocks
  private lateinit var mapper: PreviousWorkExperiencesEntityMapperImpl

  @Mock
  private lateinit var workExperienceEntityMapper: WorkExperienceEntityMapper

  @Test
  fun `should map from dto to entity`() {
    // Given
    val createPreviousWorkExperiencesDto = aValidCreatePreviousWorkExperiencesDto()
    val expectedWorkExperienceEntity = aValidWorkExperienceEntity()
    val expected = aValidPreviousWorkExperiencesEntity(
      experiences = listOf(expectedWorkExperienceEntity),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )
    given(workExperienceEntityMapper.fromDomainToEntity(any())).willReturn(expectedWorkExperienceEntity)

    // When
    val actual = mapper.fromDtoToEntity(createPreviousWorkExperiencesDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .hasAReference()
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(".*reference")
      .isEqualTo(expected)
    verify(workExperienceEntityMapper).fromDomainToEntity(createPreviousWorkExperiencesDto.experiences[0])
  }
}
