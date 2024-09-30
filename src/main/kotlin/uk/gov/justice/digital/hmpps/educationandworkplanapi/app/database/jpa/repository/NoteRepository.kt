package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.EntityType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteEntity
import java.util.UUID

@Repository
interface NoteRepository : JpaRepository<NoteEntity, UUID> {
  fun findAllByPrisonNumber(prisonNumber: String): List<NoteEntity>

  fun findAllByEntityReferenceAndEntityType(entityReference: UUID, entityType: EntityType): List<NoteEntity>

  fun findByReference(entityReference: UUID): NoteEntity
}
