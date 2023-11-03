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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousWorkExperiencesEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousWorkExperiencesEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidWorkExperienceEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidPreviousWorkExperiences
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidWorkExperience
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
      experiences = mutableListOf(expectedWorkExperienceEntity),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )
    given(workExperienceEntityMapper.fromDomainToEntity(any())).willReturn(expectedWorkExperienceEntity)

    // When
    val actual = mapper.fromCreateDtoToEntity(createPreviousWorkExperiencesDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .hasAReference()
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(".*reference")
      .isEqualTo(expected)
    verify(workExperienceEntityMapper).fromDomainToEntity(createPreviousWorkExperiencesDto.experiences[0])
  }

  @Test
  fun `should map from entity to domain`() {
    // Given
    val workExperiencesEntity = aValidPreviousWorkExperiencesEntityWithJpaFieldsPopulated()
    val expectedWorkExperience = aValidWorkExperience()
    val expectedPreviousWorkExperiences = aValidPreviousWorkExperiences(
      reference = workExperiencesEntity.reference!!,
      experiences = mutableListOf(expectedWorkExperience),
      createdAt = workExperiencesEntity.createdAt!!,
      createdAtPrison = workExperiencesEntity.createdAtPrison!!,
      createdBy = workExperiencesEntity.createdBy!!,
      createdByDisplayName = workExperiencesEntity.createdByDisplayName!!,
      lastUpdatedAt = workExperiencesEntity.updatedAt!!,
      lastUpdatedAtPrison = workExperiencesEntity.updatedAtPrison!!,
      lastUpdatedBy = workExperiencesEntity.updatedBy!!,
      lastUpdatedByDisplayName = workExperiencesEntity.updatedByDisplayName!!,
    )
    given(workExperienceEntityMapper.fromEntityToDomain(any())).willReturn(expectedWorkExperience)

    // When
    val actual = mapper.fromEntityToDomain(workExperiencesEntity)

    // Then
    assertThat(actual).isEqualTo(expectedPreviousWorkExperiences)
    verify(workExperienceEntityMapper).fromEntityToDomain(workExperiencesEntity.experiences!![0])
  }
}
