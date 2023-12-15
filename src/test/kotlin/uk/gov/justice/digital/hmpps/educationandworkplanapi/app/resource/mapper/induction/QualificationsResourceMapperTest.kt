package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.HighestEducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.QualificationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreatePreviousQualificationsRequest

class QualificationsResourceMapperTest {

  private val mapper = QualificationsResourceMapperImpl()

  @Test
  fun `should map to CreatePreviousQualificationsDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidCreatePreviousQualificationsRequest()
    val expected = aValidCreatePreviousQualificationsDto(
      educationLevel = HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
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
}
