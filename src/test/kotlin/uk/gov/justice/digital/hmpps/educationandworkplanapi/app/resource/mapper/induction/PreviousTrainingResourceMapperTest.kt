package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.TrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreatePreviousTrainingRequest

class PreviousTrainingResourceMapperTest {

  private val mapper = PreviousTrainingResourceMapperImpl()

  @Test
  fun `should map to CreatePreviousTrainingDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidCreatePreviousTrainingRequest()
    val expected = aValidCreatePreviousTrainingDto(
      trainingTypes = listOf(TrainingType.OTHER),
      trainingTypeOther = "Certified Kotlin Developer",
    )

    // When
    val actual = mapper.toCreatePreviousTrainingDto(request, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
