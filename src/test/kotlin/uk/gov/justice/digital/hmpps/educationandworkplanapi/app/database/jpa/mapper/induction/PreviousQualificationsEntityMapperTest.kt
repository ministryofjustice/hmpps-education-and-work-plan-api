package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HighestEducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.QualificationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidQualificationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreatePreviousQualificationsDto

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
      level = QualificationLevel.LEVEL_1,
      grade = "C",
    )
    val expected = aValidPreviousQualificationsEntity(
      educationLevel = HighestEducationLevel.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS,
      qualifications = listOf(expectedQualificationEntity),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )
    given(qualificationEntityMapper.fromDomainToEntity(any())).willReturn(expectedQualificationEntity)

    // When
    val actual = mapper.fromDtoToEntity(createQualificationsDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .hasAReference()
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(".*reference")
      .isEqualTo(expected)
    verify(qualificationEntityMapper).fromDomainToEntity(createQualificationsDto.qualifications[0])
  }
}
