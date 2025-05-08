package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionSummaryProjection
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionSummaryProjectionEntity
import java.util.UUID

@Repository
interface InductionSummaryProjectionRepository : JpaRepository<InductionSummaryProjectionEntity, UUID> {

  fun findByPrisonNumberIn(prisonNumbers: List<String>): List<InductionSummaryProjection>
}
