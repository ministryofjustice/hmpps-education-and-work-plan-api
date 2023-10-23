package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkExperience
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkExperienceType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPreviousWorkRequest

class PreviousWorkExperiencesResourceMapperTest {
  private val mapper = PreviousWorkExperiencesResourceMapperImpl()

  @Test
  fun `should map to PreviousTrainingDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidPreviousWorkRequest()
    val expectedExperiences = listOf(
      WorkExperience(
        experienceType = WorkExperienceType.OTHER,
        experienceTypeOther = "Scientist",
        role = "Lab Technician",
        details = "Cleaning test tubes",
      ),
    )

    // When
    val actual = mapper.toCreatePreviousWorkExperiencesDto(request, prisonId)

    // Then
    assertThat(actual!!.prisonId).isEqualTo(prisonId)
    assertThat(actual.experiences).isEqualTo(expectedExperiences)
  }
}
