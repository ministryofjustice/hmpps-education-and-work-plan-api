package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.employabilityskill.EmployabilitySkillEntity
import java.util.UUID

@Repository
interface EmployabilitySkillRepository : JpaRepository<EmployabilitySkillEntity, UUID> {
  fun findByPrisonNumber(prisonNumber: String): List<EmployabilitySkillEntity>
  fun findByPrisonNumberAndReference(prisonNumber: String, reference: UUID): EmployabilitySkillEntity?
}
