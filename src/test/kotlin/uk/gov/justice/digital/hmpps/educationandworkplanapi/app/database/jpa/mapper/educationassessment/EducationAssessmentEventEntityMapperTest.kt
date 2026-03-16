package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.educationassessment

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EducationAssessmentStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.MessageAttributes
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.SqsAssessmentEventMessage
import java.time.LocalDate

class EducationAssessmentEventEntityMapperTest {

  private val mapper = EducationAssessmentEventEntityMapper()

  @Test
  fun `should map from SQS message to entity`() {
    // Given
    val statusChangeDate = LocalDate.of(2026, 3, 15)
    val message = SqsAssessmentEventMessage(
      messageId = "14e2865f-1e4b-43b0-87e8-874e7e238dd9",
      eventType = "EducationAssessmentEventCreated",
      messageAttributes = MessageAttributes(
        prisonNumber = "A1234AB",
        status = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = statusChangeDate,
        detailUrl = "https://example.com/learnerAssessments/v2/A1234AB",
        requestId = "0650ba37-a977-4fbe-9000-4715aaecadba",
      ),
    )
    val prisonId = "BXI"

    // When
    val entity = mapper.fromMessageToEntity(message, prisonId)

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
  fun `should map from SQS message to entity with null detail URL`() {
    // Given
    val message = SqsAssessmentEventMessage(
      messageId = "14e2865f-1e4b-43b0-87e8-874e7e238dd9",
      eventType = "EducationAssessmentEventCreated",
      messageAttributes = MessageAttributes(
        prisonNumber = "A1234AB",
        status = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.now(),
        detailUrl = null,
        requestId = "0650ba37-a977-4fbe-9000-4715aaecadba",
      ),
    )

    // When
    val entity = mapper.fromMessageToEntity(message, "BXI")

    // Then
    assertThat(entity.detailUrl).isNull()
  }
}
