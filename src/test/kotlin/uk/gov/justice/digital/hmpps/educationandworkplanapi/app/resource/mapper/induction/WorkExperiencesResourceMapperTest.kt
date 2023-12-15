package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkExperienceType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidWorkExperience
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreatePreviousWorkExperiencesRequest

class WorkExperiencesResourceMapperTest {

  private val mapper = WorkExperiencesResourceMapperImpl()

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
}
