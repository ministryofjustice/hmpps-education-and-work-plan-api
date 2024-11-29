package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleEntity
import java.util.UUID

@Repository
interface ReviewScheduleRepository : JpaRepository<ReviewScheduleEntity, UUID> {
  fun findByReference(reference: UUID): ReviewScheduleEntity?

  fun getAllByPrisonNumber(prisonNumber: String): List<ReviewScheduleEntity>

  @Query("select rse from ReviewScheduleEntity rse where rse.prisonNumber = :prisonNumber and rse.scheduleStatus != 'COMPLETED'")
  fun findActiveReviewSchedule(prisonNumber: String): ReviewScheduleEntity?

  fun findFirstByPrisonNumberOrderByUpdatedAtDesc(prisonNumber: String): ReviewScheduleEntity?

  fun findByPrisonNumberIn(prisonNumbers: List<String>): List<ReviewScheduleEntity>
}
