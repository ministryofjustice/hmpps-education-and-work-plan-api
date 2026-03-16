package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.educationassessment

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EducationAssessmentStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.SqsAssessmentEventMessage
import java.util.UUID

@Component
class EducationAssessmentEventEntityMapper {

  fun fromMessageToEntity(
    message: SqsAssessmentEventMessage,
    prisonId: String,
  ): EducationAssessmentEventEntity = with(message.messageAttributes) {
    EducationAssessmentEventEntity(
      reference = UUID.randomUUID(),
      prisonNumber = prisonNumber,
      status = status.toEntityStatus(),
      statusChangeDate = statusChangeDate,
      source = "CURIOUS",
      detailUrl = detailUrl,
      createdAtPrison = prisonId,
      updatedAtPrison = prisonId,
    )
  }
}

private fun EducationAssessmentStatus.toEntityStatus(): EducationAssessmentEventStatus = when (this) {
  EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE -> EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE
}
