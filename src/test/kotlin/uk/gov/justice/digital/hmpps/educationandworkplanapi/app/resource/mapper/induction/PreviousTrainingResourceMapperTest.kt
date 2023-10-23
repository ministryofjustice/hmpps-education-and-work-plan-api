package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.TrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidEducationAndQualificationsRequest

class PreviousTrainingResourceMapperTest {
  private val mapper = PreviousTrainingResourceMapperImpl()

  @Test
  fun `should map to PreviousTrainingDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidEducationAndQualificationsRequest()
    val expectedTrainingTypes = listOf(TrainingType.CSCS_CARD, TrainingType.OTHER)

    // When
    val actual = mapper.toCreatePreviousTrainingDto(request, prisonId)

    // Then
    assertThat(actual!!.prisonId).isEqualTo(prisonId)
    assertThat(actual.trainingTypes).isEqualTo(expectedTrainingTypes)
    assertThat(actual.trainingTypeOther).isEqualTo("Any training")
  }
}
