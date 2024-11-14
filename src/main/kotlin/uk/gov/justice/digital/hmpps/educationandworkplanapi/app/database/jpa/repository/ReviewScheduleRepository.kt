package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleEntity
import java.util.UUID

@Repository
interface ReviewScheduleRepository : JpaRepository<ReviewScheduleEntity, UUID> {
  fun findByReference(reference: UUID): ReviewScheduleEntity?

  fun getAllByPrisonNumber(prisonNumber: String): List<ReviewScheduleEntity>
}
