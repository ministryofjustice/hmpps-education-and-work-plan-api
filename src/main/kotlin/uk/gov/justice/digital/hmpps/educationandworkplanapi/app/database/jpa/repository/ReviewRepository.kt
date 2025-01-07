package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewEntity
import java.util.UUID

@Repository
interface ReviewRepository : JpaRepository<ReviewEntity, UUID> {
  fun getAllByPrisonNumber(prisonNumber: String): List<ReviewEntity>

  @Modifying
  @Query("UPDATE ReviewEntity SET preRelease = TRUE WHERE reference = :reference")
  fun setPreRelease(reference: UUID)
}
