package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.education

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.aValidAchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.aValidAchievedQualificationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.aValidAchievedQualificationToCreate
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.aValidAchievedQualificationToUpdate
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.aValidCreateEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.aValidEducationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.aValidUpdateEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.assertThat
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationLevel as EducationLevelDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.QualificationLevel as QualificationLevelDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel as EducationLevelApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.QualificationLevel as QualificationLevelApi

@ExtendWith(MockitoExtension::class)
class EducationResourceMapperTest {

  @InjectMocks
  private lateinit var educationResourceMapper: EducationResourceMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Mock
  private lateinit var userService: ManageUserService

  @Test
  fun `should map PreviousQualifications to EducationResponse`() {
    // Given
    val educationReference = UUID.randomUUID()
    val qualificationReference = UUID.randomUUID()

    val previousQualifications = aValidPreviousQualifications(
      reference = educationReference,
      educationLevel = EducationLevelDomain.FURTHER_EDUCATION_COLLEGE,
      createdBy = "asmith_gen",
      createdByDisplayName = "Alex Smith",
      createdAt = Instant.parse("2024-08-12T09:32:45.123Z"),
      createdAtPrison = "BXI",
      lastUpdatedBy = "bjones_gen",
      lastUpdatedByDisplayName = "Barry Jones",
      lastUpdatedAt = Instant.parse("2024-08-12T10:03:34.987Z"),
      lastUpdatedAtPrison = "BXI",
      qualifications = listOf(
        aValidQualification(
          reference = qualificationReference,
          subject = "English",
          level = QualificationLevelDomain.LEVEL_1,
          grade = "C",
          createdBy = "asmith_gen",
          createdAt = Instant.parse("2024-08-12T09:32:45.123Z"),
          lastUpdatedBy = "bjones_gen",
          lastUpdatedAt = Instant.parse("2024-08-12T10:03:34.987Z"),
        ),
      ),
    )

    val expectedDateTime = OffsetDateTime.now()
    given(instantMapper.toOffsetDateTime(any())).willReturn(expectedDateTime)
    given(userService.getUserDetails(any())).willReturn(
      UserDetailsDto("asmith_gen", true, "Alex Smith"),
      UserDetailsDto("bjones_gen", true, "Barry Jones"),
    )

    val expected = aValidEducationResponse(
      reference = educationReference,
      educationLevel = EducationLevelApi.FURTHER_EDUCATION_COLLEGE,
      createdBy = "asmith_gen",
      createdByDisplayName = "Alex Smith",
      createdAt = expectedDateTime,
      createdAtPrison = "BXI",
      updatedBy = "bjones_gen",
      updatedByDisplayName = "Barry Jones",
      updatedAt = expectedDateTime,
      updatedAtPrison = "BXI",
      qualifications = listOf(
        aValidAchievedQualificationResponse(
          reference = qualificationReference,
          subject = "English",
          level = QualificationLevelApi.LEVEL_1,
          grade = "C",
          createdBy = "asmith_gen",
          createdAt = expectedDateTime,
          updatedBy = "bjones_gen",
          updatedAt = expectedDateTime,
        ),
      ),
    )

    // When
    val actual = educationResourceMapper.toEducationResponse(previousQualifications)

    // Then
    assertThat(actual).isEqualTo(expected)
    verify(userService).getUserDetails("asmith_gen")
    verify(userService).getUserDetails("bjones_gen")
  }

  @Test
  fun `should map CreateEducationRequest to CreatePreviousQualificationsDto`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createEducationRequest = aValidCreateEducationRequest(
      prisonId = "BXI",
      educationLevel = EducationLevelApi.FURTHER_EDUCATION_COLLEGE,
      qualifications = listOf(
        aValidAchievedQualification(
          subject = "Pottery",
          level = QualificationLevelApi.LEVEL_4,
          grade = "A*",
        ),
      ),
    )

    val expected = aValidCreatePreviousQualificationsDto(
      prisonNumber = prisonNumber,
      prisonId = "BXI",
      educationLevel = EducationLevelDomain.FURTHER_EDUCATION_COLLEGE,
      qualifications = listOf(
        aValidCreateQualificationDto(
          subject = "Pottery",
          level = QualificationLevelDomain.LEVEL_4,
          grade = "A*",
        ),
      ),
    )

    // When
    val actual = educationResourceMapper.toCreatePreviousQualificationsDto(prisonNumber, createEducationRequest)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should map UpdateEducationRequest to UpdatePreviousQualificationsDto`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val qualificationsRecordReference = UUID.randomUUID()
    val potteryQualificationReference = UUID.randomUUID()

    val updateEducationRequest = aValidUpdateEducationRequest(
      reference = qualificationsRecordReference,
      prisonId = "BXI",
      educationLevel = EducationLevelApi.FURTHER_EDUCATION_COLLEGE,
      qualifications = listOf(
        aValidAchievedQualificationToUpdate(
          reference = potteryQualificationReference,
          subject = "Pottery",
          level = QualificationLevelApi.LEVEL_4,
          grade = "A*",
        ),
        aValidAchievedQualificationToCreate(
          subject = "Maths",
          level = QualificationLevelApi.LEVEL_3,
          grade = "C",
        ),
      ),
    )

    val expected = aValidUpdatePreviousQualificationsDto(
      reference = qualificationsRecordReference,
      prisonNumber = prisonNumber,
      prisonId = "BXI",
      educationLevel = EducationLevelDomain.FURTHER_EDUCATION_COLLEGE,
      qualifications = listOf(
        aValidUpdateQualificationDto(
          reference = potteryQualificationReference,
          subject = "Pottery",
          level = QualificationLevelDomain.LEVEL_4,
          grade = "A*",
        ),
        aValidCreateQualificationDto(
          subject = "Maths",
          level = QualificationLevelDomain.LEVEL_3,
          grade = "C",
        ),
      ),
    )

    // When
    val actual = educationResourceMapper.toUpdatePreviousQualificationsDto(prisonNumber, updateEducationRequest)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
