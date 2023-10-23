package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.HighestEducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Qualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.QualificationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidEducationAndQualificationsRequest

class PreviousQualificationsResourceMapperTest {
  private val mapper = PreviousQualificationsResourceMapperImpl()

  @Test
  fun `should map to PreviousQualificationsDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidEducationAndQualificationsRequest()
    val expectedQualifications = listOf(
      Qualification(
        subject = "English",
        level = QualificationLevel.LEVEL_3,
        grade = "A",
      ),
      Qualification(
        subject = "Maths",
        level = QualificationLevel.LEVEL_3,
        grade = "B",
      ),
    )

    // When
    val actual = mapper.toCreatePreviousQualificationsDto(request, prisonId)

    // Then
    assertThat(actual!!.prisonId).isEqualTo(prisonId)
    assertThat(actual.educationLevel).isEqualTo(HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS)
    assertThat(actual.qualifications).isEqualTo(expectedQualifications)
  }
}
