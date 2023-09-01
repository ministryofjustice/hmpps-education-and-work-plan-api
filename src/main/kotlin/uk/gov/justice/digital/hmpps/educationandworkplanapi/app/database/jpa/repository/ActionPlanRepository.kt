package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.ActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.ActionPlanSummaryProjection
import java.util.UUID

@Repository
interface ActionPlanRepository : JpaRepository<ActionPlanEntity, UUID> {

  fun findByPrisonNumberIn(prisonNumbers: List<String>): List<ActionPlanSummaryProjection>

  fun findByPrisonNumber(prisonNumber: String): ActionPlanEntity?
}
