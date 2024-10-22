package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkExperienceType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidPreviousWorkExperiences
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidWorkExperience
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.UserDetailsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreatePreviousWorkExperiencesRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPreviousWorkExperience
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPreviousWorkExperiencesResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdatePreviousWorkExperiencesRequest
import java.time.OffsetDateTime

@ExtendWith(MockitoExtension::class)
class WorkExperiencesResourceMapperTest {

  @InjectMocks
  private lateinit var mapper: WorkExperiencesResourceMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Mock
  private lateinit var userService: ManageUserService

  @Test
  fun `should map to CreatePreviousWorkExperiencesDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidCreatePreviousWorkExperiencesRequest()
    val expected = aValidCreatePreviousWorkExperiencesDto(
      experiences = listOf(
        aValidWorkExperience(
          experienceType = WorkExperienceType.OTHER,
          experienceTypeOther = "Scientist",
          role = "Lab Technician",
          details = "Cleaning test tubes",
        ),
      ),
    )

    // When
    val actual = mapper.toCreatePreviousWorkExperiencesDto(request, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should map to PreviousWorkExperiencesResponse`() {
    // Given
    val domain = aValidPreviousWorkExperiences()

    given(userService.getUserDetails(any())).willReturn(
      UserDetailsDto("asmith_gen", true, "Alex Smith"),
      UserDetailsDto("bjones_gen", true, "Barry Jones"),
    )

    val expectedDateTime = OffsetDateTime.now()
    val expected = aValidPreviousWorkExperiencesResponse(
      reference = domain.reference,
      experiences = listOf(
        aValidPreviousWorkExperience(
          experienceType = WorkType.OTHER,
          experienceTypeOther = "All sorts",
          role = "General dog's body",
          details = null,
        ),
      ),
      createdAt = expectedDateTime,
      createdBy = "asmith_gen",
      createdByDisplayName = "Alex Smith",
      updatedAt = expectedDateTime,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "Barry Jones",
    )
    given(instantMapper.toOffsetDateTime(any())).willReturn(expectedDateTime)

    // When
    val actual = mapper.toPreviousWorkExperiencesResponse(domain)

    // Then
    assertThat(actual).isEqualTo(expected)
    verify(userService).getUserDetails("asmith_gen")
    verify(userService).getUserDetails("bjones_gen")
  }

  @Test
  fun `should map to UpdatePreviousWorkExperiencesDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidUpdatePreviousWorkExperiencesRequest()
    val expected = aValidUpdatePreviousWorkExperiencesDto(
      reference = request.reference!!,
      experiences = listOf(
        aValidWorkExperience(
          experienceType = WorkExperienceType.OTHER,
          experienceTypeOther = "Scientist",
          role = "Lab Technician",
          details = "Cleaning test tubes",
        ),
      ),
    )

    // When
    val actual = mapper.toUpdatePreviousWorkExperiencesDto(request, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
