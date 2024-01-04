package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.QualificationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidPreviousQualifications
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidAchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreatePreviousQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPreviousQualificationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.anotherValidAchievedQualification
import java.time.OffsetDateTime
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.HighestEducationLevel as HighestEducationLevelDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HighestEducationLevel as HighestEducationLevelApi

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
    val request = aValidCreatePreviousQualificationsRequest()
    val expected = aValidCreatePreviousQualificationsDto(
      educationLevel = HighestEducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = listOf(
        aValidQualification(
          subject = "English",
          level = QualificationLevel.LEVEL_3,
          grade = "A",
        ),
        aValidQualification(
          subject = "Maths",
          level = QualificationLevel.LEVEL_3,
          grade = "B",
        ),
      ),
    )

    // When
    val actual = mapper.toCreatePreviousQualificationsDto(request, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should map to PreviousQualificationsResponse`() {
    // Given
    val domain = aValidPreviousQualifications(
      educationLevel = HighestEducationLevelDomain.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS,
      qualifications = listOf(
        aValidQualification(
          subject = "English",
          level = QualificationLevel.LEVEL_3,
          grade = "A",
        ),
        aValidQualification(
          subject = "Maths",
          level = QualificationLevel.LEVEL_4,
          grade = "B",
        ),
      ),
    )
    val expectedDateTime = OffsetDateTime.now()
    val expected = aValidPreviousQualificationsResponse(
      reference = domain.reference,
      educationLevel = HighestEducationLevelApi.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS,
      qualifications = listOf(
        aValidAchievedQualification(
          subject = "English",
          level = AchievedQualification.Level.LEVEL_3,
          grade = "A",
        ),
        anotherValidAchievedQualification(
          subject = "Maths",
          level = AchievedQualification.Level.LEVEL_4,
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
}
