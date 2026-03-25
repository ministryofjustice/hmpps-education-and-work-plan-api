package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.educationassessment

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.AssessmentEventDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.AssessmentEventStatus
import java.time.LocalDate

class EducationAssessmentEventEntityMapperTest {

  private val mapper = EducationAssessmentEventEntityMapper()

  @Test
  fun `should map from DTO to entity`() {
    // Given
    val statusChangeDate = LocalDate.of(2026, 3, 15)
    val dto = AssessmentEventDto(
      prisonNumber = "A1234AB",
      status = AssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
      statusChangeDate = statusChangeDate,
      detailUrl = "https://example.com/learnerAssessments/v2/A1234AB",
    )
    val prisonId = "BXI"

    // When
    val entity = mapper.fromDtoToEntity(dto, prisonId)

    // Then
    assertThat(entity.reference).isNotNull()
    assertThat(entity.prisonNumber).isEqualTo("A1234AB")
    assertThat(entity.status).isEqualTo(EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE)
    assertThat(entity.statusChangeDate).isEqualTo(statusChangeDate)
    assertThat(entity.source).isEqualTo("CURIOUS")
    assertThat(entity.detailUrl).isEqualTo("https://example.com/learnerAssessments/v2/A1234AB")
    assertThat(entity.createdAtPrison).isEqualTo("BXI")
    assertThat(entity.updatedAtPrison).isEqualTo("BXI")
    // JPA managed fields should not be populated
    assertThat(entity.id).isNull()
    assertThat(entity.createdAt).isNull()
    assertThat(entity.createdBy).isNull()
    assertThat(entity.updatedAt).isNull()
    assertThat(entity.updatedBy).isNull()
  }

  @Test
  fun `should map from DTO to entity with null detail URL`() {
    // Given
    val dto = AssessmentEventDto(
      prisonNumber = "A1234AB",
      status = AssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
      statusChangeDate = LocalDate.now(),
      detailUrl = null,
    )

    // When
    val entity = mapper.fromDtoToEntity(dto, "BXI")

    // Then
    assertThat(entity.detailUrl).isNull()
  }
}
