package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionEntity
import java.util.UUID

@Repository
interface InductionRepository : JpaRepository<InductionEntity, UUID> {
  fun findByPrisonNumber(prisonNumber: String): InductionEntity?
}
