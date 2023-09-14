package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.TimelineEntity
import java.util.UUID

@Repository
interface TimelineRepository : JpaRepository<TimelineEntity, UUID> {
  fun findByPrisonNumber(prisonNumber: String): TimelineEntity?
}
