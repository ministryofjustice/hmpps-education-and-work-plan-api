package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleHistoryEntity
import java.util.UUID

@Repository
interface ReviewScheduleHistoryRepository : JpaRepository<ReviewScheduleHistoryEntity, Long> {
  @Query("SELECT MAX(h.version) FROM ReviewScheduleHistoryEntity h WHERE h.reference = :reviewScheduleReference")
  fun findMaxVersionByReviewScheduleReference(reviewScheduleReference: UUID): Int?

  fun findAllByReference(reviewScheduleReference: UUID): List<ReviewScheduleHistoryEntity>

  fun findAllByPrisonNumber(prisonNumber: String): List<ReviewScheduleHistoryEntity>
}
