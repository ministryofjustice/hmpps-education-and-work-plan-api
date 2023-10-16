package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalEntity
import java.util.UUID

@Repository
interface GoalRepository : JpaRepository<GoalEntity, UUID> {

  fun findByReference(reference: UUID): GoalEntity?
}
