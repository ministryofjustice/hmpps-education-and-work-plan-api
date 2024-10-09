package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.EntityType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteType
import java.util.UUID

@Repository
interface NoteRepository : JpaRepository<NoteEntity, UUID> {
  fun findAllByPrisonNumber(prisonNumber: String): List<NoteEntity>

  fun findAllByEntityReferenceAndEntityType(
    entityReference: UUID,
    entityType: EntityType,
  ): List<NoteEntity>

  fun findAllByEntityReferenceAndEntityTypeAndNoteType(
    entityReference: UUID,
    entityType: EntityType,
    noteType: NoteType,
  ): List<NoteEntity>

  fun findByReference(entityReference: UUID): NoteEntity

  fun findAllByEntityReference(entityReference: UUID): NoteEntity

  @Modifying
  @Query("delete NoteEntity n where n.entityReference = :entityReference and n.entityType = :entityType and n.noteType = :noteType")
  fun deleteNoteEntityByEntityReferenceAndEntityTypeAndNoteType(
    entityReference: UUID,
    entityType: EntityType,
    noteType: NoteType,
  )
}
