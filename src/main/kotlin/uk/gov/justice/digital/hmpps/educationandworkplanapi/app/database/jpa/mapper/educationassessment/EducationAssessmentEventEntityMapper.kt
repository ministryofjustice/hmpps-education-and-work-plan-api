package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.educationassessment

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.AssessmentEventDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.AssessmentEventStatus
import java.util.UUID

@Component
class EducationAssessmentEventEntityMapper {

  fun fromDtoToEntity(
    dto: AssessmentEventDto,
    prisonId: String,
  ): EducationAssessmentEventEntity = EducationAssessmentEventEntity(
    reference = UUID.randomUUID(),
    prisonNumber = dto.prisonNumber,
    status = dto.status.toEntityStatus(),
    statusChangeDate = dto.statusChangeDate,
    source = "CURIOUS",
    detailUrl = dto.detailUrl,
    createdAtPrison = prisonId,
    updatedAtPrison = prisonId,
  )
}

private fun AssessmentEventStatus.toEntityStatus(): EducationAssessmentEventStatus = when (this) {
  AssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE -> EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE
}
