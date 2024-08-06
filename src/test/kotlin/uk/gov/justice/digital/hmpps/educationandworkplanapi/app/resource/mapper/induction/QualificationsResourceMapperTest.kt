package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidPreviousQualifications
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidQualification
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidAchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreatePreviousQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPreviousQualificationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdatePreviousQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.anotherValidAchievedQualification
import java.time.OffsetDateTime
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.EducationLevel as EducationLevelDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.QualificationLevel as QualificationLevelDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel as EducationLevelApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.QualificationLevel as QualificationLevelApi

@ExtendWith(MockitoExtension::class)
class QualificationsResourceMapperTest {

  @InjectMocks
  private lateinit var mapper: QualificationsResourceMapperImpl

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Test
  fun `should map to CreatePreviousQualificationsDto`() {
    // Given
    val prisonId = "BXI"
    val prisonNumber = "A1234BC"
    val request = aValidCreatePreviousQualificationsRequest()
    val expected = aValidCreatePreviousQualificationsDto(
      prisonNumber = "A1234BC",
      educationLevel = EducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = listOf(
        aValidQualification(
          subject = "English",
          level = QualificationLevelDomain.LEVEL_3,
          grade = "A",
        ),
        aValidQualification(
          subject = "Maths",
          level = QualificationLevelDomain.LEVEL_3,
          grade = "B",
        ),
      ),
    )

    // When
    val actual = mapper.toCreatePreviousQualificationsDto(request, prisonNumber, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should map to PreviousQualificationsResponse`() {
    // Given
    val domain = aValidPreviousQualifications(
      educationLevel = EducationLevelDomain.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS,
      qualifications = listOf(
        aValidQualification(
          subject = "English",
          level = QualificationLevelDomain.LEVEL_3,
          grade = "A",
        ),
        aValidQualification(
          subject = "Maths",
          level = QualificationLevelDomain.LEVEL_4,
          grade = "B",
        ),
      ),
    )
    val expectedDateTime = OffsetDateTime.now()
    val expected = aValidPreviousQualificationsResponse(
      reference = domain.reference,
      educationLevel = EducationLevelApi.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS,
      qualifications = listOf(
        aValidAchievedQualification(
          subject = "English",
          level = QualificationLevelApi.LEVEL_3,
          grade = "A",
        ),
        anotherValidAchievedQualification(
          subject = "Maths",
          level = QualificationLevelApi.LEVEL_4,
          grade = "B",
        ),
      ),
      createdAt = expectedDateTime,
      updatedAt = expectedDateTime,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "Barry Jones",
    )
    given(instantMapper.toOffsetDateTime(any())).willReturn(expectedDateTime)

    // When
    val actual = mapper.toPreviousQualificationsResponse(domain)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should map to UpdatePreviousQualificationsDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidUpdatePreviousQualificationsRequest()
    val expected = aValidUpdatePreviousQualificationsDto(
      reference = request.reference!!,
      educationLevel = EducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = listOf(
        aValidQualification(
          subject = "English",
          level = QualificationLevelDomain.LEVEL_3,
          grade = "A",
        ),
        aValidQualification(
          subject = "Maths",
          level = QualificationLevelDomain.LEVEL_3,
          grade = "B",
        ),
      ),
    )

    // When
    val actual = mapper.toUpdatePreviousQualificationsDto(request, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
