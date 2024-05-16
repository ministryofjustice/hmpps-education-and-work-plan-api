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
import uk.gov.justice.digital.hmpps.domain.induction.aValidPreviousQualifications
import uk.gov.justice.digital.hmpps.domain.induction.aValidQualification
import uk.gov.justice.digital.hmpps.domain.induction.dto.aValidCreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousQualificationsEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidQualificationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.domain.induction.HighestEducationLevel as HighestEducationLevelDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HighestEducationLevel as HighestEducationLevelEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.QualificationLevel as QualificationLevelEntity

@ExtendWith(MockitoExtension::class)
class PreviousQualificationsEntityMapperTest {
  @InjectMocks
  private lateinit var mapper: PreviousQualificationsEntityMapperImpl

  @Mock
  private lateinit var qualificationEntityMapper: QualificationEntityMapper

  @Test
  fun `should map from dto to entity`() {
    // Given
    val createQualificationsDto = aValidCreatePreviousQualificationsDto()
    val expectedQualificationEntity = aValidQualificationEntity(
      subject = "English",
      level = QualificationLevelEntity.LEVEL_1,
      grade = "C",
    )
    val expected = aValidPreviousQualificationsEntity(
      educationLevel = HighestEducationLevelEntity.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS,
      qualifications = mutableListOf(expectedQualificationEntity),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )
    given(qualificationEntityMapper.fromDomainToEntity(any())).willReturn(expectedQualificationEntity)

    // When
    val actual = mapper.fromCreateDtoToEntity(createQualificationsDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .hasAReference()
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(".*reference")
      .isEqualTo(expected)
    verify(qualificationEntityMapper).fromDomainToEntity(createQualificationsDto.qualifications[0])
  }

  @Test
  fun `should map from entity to domain`() {
    // Given
    val qualificationsEntity = aValidPreviousQualificationsEntityWithJpaFieldsPopulated()
    val expectedQualification = aValidQualification()
    val expectedPreviousQualifications = aValidPreviousQualifications(
      reference = qualificationsEntity.reference!!,
      educationLevel = HighestEducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = mutableListOf(expectedQualification),
      createdAt = qualificationsEntity.createdAt!!,
      createdAtPrison = qualificationsEntity.createdAtPrison!!,
      createdBy = qualificationsEntity.createdBy!!,
      createdByDisplayName = qualificationsEntity.createdByDisplayName!!,
      lastUpdatedAt = qualificationsEntity.updatedAt!!,
      lastUpdatedAtPrison = qualificationsEntity.updatedAtPrison!!,
      lastUpdatedBy = qualificationsEntity.updatedBy!!,
      lastUpdatedByDisplayName = qualificationsEntity.updatedByDisplayName!!,
    )
    given(qualificationEntityMapper.fromEntityToDomain(any())).willReturn(expectedQualification)

    // When
    val actual = mapper.fromEntityToDomain(qualificationsEntity)

    // Then
    assertThat(actual).isEqualTo(expectedPreviousQualifications)
    verify(qualificationEntityMapper).fromEntityToDomain(qualificationsEntity.qualifications!![0])
  }
}
