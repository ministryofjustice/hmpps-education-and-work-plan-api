package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleHistoryEntity
import java.util.UUID

@Repository
interface InductionScheduleHistoryRepository : JpaRepository<InductionScheduleHistoryEntity, Long> {
  @Query("SELECT MAX(h.version) FROM InductionScheduleHistoryEntity h WHERE h.reference = :inductionScheduleReference")
  fun findMaxVersionByInductionScheduleReference(inductionScheduleReference: UUID): Int?

  fun findAllByReference(inductionScheduleReference: UUID): List<InductionScheduleHistoryEntity>

  fun findAllByPrisonNumber(prisonNumber: String): List<InductionScheduleHistoryEntity>
}
