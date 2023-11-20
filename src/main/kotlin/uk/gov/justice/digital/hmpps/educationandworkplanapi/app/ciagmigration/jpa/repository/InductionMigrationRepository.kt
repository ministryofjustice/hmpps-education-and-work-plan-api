package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.induction.InductionMigrationEntity
import java.util.UUID

@Repository
interface InductionMigrationRepository : JpaRepository<InductionMigrationEntity, UUID> {
  fun findByPrisonNumber(prisonNumber: String): InductionMigrationEntity?
}
