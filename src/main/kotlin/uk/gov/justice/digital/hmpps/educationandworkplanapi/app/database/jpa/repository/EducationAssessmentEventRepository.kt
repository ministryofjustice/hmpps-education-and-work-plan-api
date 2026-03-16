package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventEntity
import java.util.UUID

@Repository
interface EducationAssessmentEventRepository : JpaRepository<EducationAssessmentEventEntity, UUID> {
  fun findByPrisonNumber(prisonNumber: String): List<EducationAssessmentEventEntity>
}
