package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleEntity
import java.util.UUID

@Repository
interface InductionScheduleRepository : JpaRepository<InductionScheduleEntity, UUID> {
  fun findByReference(reference: UUID): InductionScheduleEntity?

  fun findByPrisonNumber(prisonNumber: String): InductionScheduleEntity?
}
