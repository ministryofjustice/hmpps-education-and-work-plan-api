package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.aValidPreviousQualifications
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.aValidQualification
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.aValidCreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.aValidCreateQualificationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.aValidUpdatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.aValidUpdateQualificationDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.UserDetailsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.aValidAchievedQualificationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.anotherValidAchievedQualificationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidAchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreatePreviousQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPreviousQualificationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdatePreviousQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.anotherValidAchievedQualification
import java.time.OffsetDateTime
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationLevel as EducationLevelDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.QualificationLevel as QualificationLevelDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel as EducationLevelApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.QualificationLevel as QualificationLevelApi

@ExtendWith(MockitoExtension::class)
class QualificationsResourceMapperTest {

  @InjectMocks
  private lateinit var mapper: QualificationsResourceMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Mock
  private lateinit var userService: ManageUserService

  @Test
  fun `should map to CreatePreviousQualificationsDto given qualification without a reference`() {
    // Given
    val prisonId = "BXI"
    val prisonNumber = "A1234BC"
    val request = aValidCreatePreviousQualificationsRequest(
      qualifications = listOf(aValidAchievedQualification(reference = null)),
    )

    val expected = aValidCreatePreviousQualificationsDto(
      prisonNumber = "A1234BC",
      educationLevel = EducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = listOf(
        aValidCreateQualificationDto(
          subject = "English",
          level = QualificationLevelDomain.LEVEL_3,
          grade = "A",
        ),
      ),
    )

    // When
    val actual = mapper.toCreatePreviousQualificationsDto(request, prisonNumber, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should map to CreatePreviousQualificationsDto given qualifications with and without a reference`() {
    // Given
    val prisonId = "BXI"
    val prisonNumber = "A1234BC"
    val existingQualificationReference = UUID.randomUUID()
    val request = aValidCreatePreviousQualificationsRequest(
      qualifications = listOf(
        aValidAchievedQualification(reference = existingQualificationReference),
        anotherValidAchievedQualification(reference = null),
      ),
    )

    val expected = aValidCreatePreviousQualificationsDto(
      prisonNumber = "A1234BC",
      educationLevel = EducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = listOf(
        aValidUpdateQualificationDto(
          reference = existingQualificationReference,
          subject = "English",
          level = QualificationLevelDomain.LEVEL_3,
          grade = "A",
        ),
        aValidCreateQualificationDto(
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
    given(userService.getUserDetails(any())).willReturn(
      UserDetailsDto("asmith_gen", true, "Alex Smith"),
      UserDetailsDto("bjones_gen", true, "Barry Jones"),
    )
    val domain = aValidPreviousQualifications(
      reference = UUID.fromString("52ac8188-f372-4a7a-8de1-14b8160f0a2b"),
      educationLevel = EducationLevelDomain.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS,
      qualifications = listOf(
        aValidQualification(
          reference = UUID.fromString("c823568e-efe4-42fc-9a4e-227eb4e69ad0"),
          subject = "English",
          level = QualificationLevelDomain.LEVEL_3,
          grade = "A",
          createdBy = "asmith_gen",
          lastUpdatedBy = "bjones_gen",
        ),
        aValidQualification(
          reference = UUID.fromString("371c25f7-ec55-4323-b296-11a643f12307"),
          subject = "Maths",
          level = QualificationLevelDomain.LEVEL_4,
          grade = "B",
          createdBy = "fbloggs_gen",
          lastUpdatedBy = "fnorth_gen",
        ),
      ),
    )

    val expectedDateTime = OffsetDateTime.now()
    given(instantMapper.toOffsetDateTime(any())).willReturn(expectedDateTime)

    val expected = aValidPreviousQualificationsResponse(
      reference = UUID.fromString("52ac8188-f372-4a7a-8de1-14b8160f0a2b"),
      educationLevel = EducationLevelApi.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS,
      qualifications = listOf(
        aValidAchievedQualificationResponse(
          reference = UUID.fromString("c823568e-efe4-42fc-9a4e-227eb4e69ad0"),
          subject = "English",
          level = QualificationLevelApi.LEVEL_3,
          grade = "A",
          createdBy = "asmith_gen",
          createdAt = expectedDateTime,
          updatedBy = "bjones_gen",
          updatedAt = expectedDateTime,
        ),
        anotherValidAchievedQualificationResponse(
          reference = UUID.fromString("371c25f7-ec55-4323-b296-11a643f12307"),
          subject = "Maths",
          level = QualificationLevelApi.LEVEL_4,
          grade = "B",
          createdAt = expectedDateTime,
          createdBy = "fbloggs_gen",
          updatedAt = expectedDateTime,
          updatedBy = "fnorth_gen",
        ),
      ),
      createdAt = expectedDateTime,
      updatedAt = expectedDateTime,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "Barry Jones",
    )

    // When
    val actual = mapper.toPreviousQualificationsResponse(domain)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should map to UpdatePreviousQualificationsDto given qualification without a reference`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val prisonId = "BXI"
    val request = aValidUpdatePreviousQualificationsRequest(
      qualifications = listOf(
        aValidAchievedQualification(reference = null),
      ),
    )

    val expected = aValidUpdatePreviousQualificationsDto(
      reference = request.reference!!,
      prisonId = prisonId,
      prisonNumber = prisonNumber,
      educationLevel = EducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = listOf(
        aValidCreateQualificationDto(
          subject = "English",
          level = QualificationLevelDomain.LEVEL_3,
          grade = "A",
        ),
      ),
    )

    // When
    val actual = mapper.toUpdatePreviousQualificationsDto(
      request = request,
      prisonId = prisonId,
      prisonNumber = prisonNumber,
    )

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should map to UpdatePreviousQualificationsDto given new qualification without a reference`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val prisonId = "BXI"
    val existingQualificationReference = UUID.randomUUID()
    val request = aValidUpdatePreviousQualificationsRequest(
      qualifications = listOf(
        aValidAchievedQualification(reference = existingQualificationReference),
        anotherValidAchievedQualification(reference = null),
      ),
    )

    val expected = aValidUpdatePreviousQualificationsDto(
      reference = request.reference!!,
      prisonId = prisonId,
      prisonNumber = prisonNumber,
      educationLevel = EducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS,
      qualifications = listOf(
        aValidUpdateQualificationDto(
          reference = existingQualificationReference,
          subject = "English",
          level = QualificationLevelDomain.LEVEL_3,
          grade = "A",
        ),
        aValidCreateQualificationDto(
          subject = "Maths",
          level = QualificationLevelDomain.LEVEL_3,
          grade = "B",
        ),
      ),
    )

    // When
    val actual = mapper.toUpdatePreviousQualificationsDto(
      request = request,
      prisonId = prisonId,
      prisonNumber = prisonNumber,
    )

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
