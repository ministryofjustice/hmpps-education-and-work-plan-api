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
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkExperience
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidPreviousWorkExperiences
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidWorkExperience
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkExperienceEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousWorkExperiencesEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousWorkExperiencesEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidWorkExperienceEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat

@ExtendWith(MockitoExtension::class)
class PreviousWorkExperiencesEntityMapperTest {
  @InjectMocks
  private lateinit var mapper: PreviousWorkExperiencesEntityMapper

  @Mock
  private lateinit var workExperienceEntityMapper: WorkExperienceEntityMapper

  @Mock
  private lateinit var entityListManager: InductionEntityListManager<WorkExperienceEntity, WorkExperience>

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
      reference = workExperiencesEntity.reference,
      experiences = mutableListOf(expectedWorkExperience),
      createdAt = workExperiencesEntity.createdAt!!,
      createdAtPrison = workExperiencesEntity.createdAtPrison,
      createdBy = workExperiencesEntity.createdBy!!,
      lastUpdatedAt = workExperiencesEntity.updatedAt!!,
      lastUpdatedAtPrison = workExperiencesEntity.updatedAtPrison,
      lastUpdatedBy = workExperiencesEntity.updatedBy!!,
    )
    given(workExperienceEntityMapper.fromEntityToDomain(any())).willReturn(expectedWorkExperience)

    // When
    val actual = mapper.fromEntityToDomain(workExperiencesEntity)

    // Then
    assertThat(actual).isEqualTo(expectedPreviousWorkExperiences)
    verify(workExperienceEntityMapper).fromEntityToDomain(workExperiencesEntity.experiences[0])
  }
}
