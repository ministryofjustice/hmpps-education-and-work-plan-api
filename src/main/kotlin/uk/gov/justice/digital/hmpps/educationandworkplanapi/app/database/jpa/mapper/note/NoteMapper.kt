package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.note

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.CreateNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteEntity
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.EntityType as DomainEntityType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteType as DomainNoteType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.EntityType as EntityEntityType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteType as EntityNoteType

object NoteMapper {

  fun fromDomainToEntity(createNoteDto: CreateNoteDto): NoteEntity = NoteEntity(
    reference = UUID.randomUUID(),
    prisonNumber = createNoteDto.prisonNumber,
    content = createNoteDto.content,
    noteType = fromDomainNoteTypeToEntityNoteType(createNoteDto.noteType),
    entityType = fromDomainEntityTypeToEntityEntityType(createNoteDto.entityType),
    entityReference = createNoteDto.entityReference,
    createdAtPrison = createNoteDto.createdAtPrison,
    updatedAtPrison = createNoteDto.lastUpdatedAtPrison,
  )

  fun fromEntityToDomain(noteEntity: NoteEntity): NoteDto = NoteDto(
    reference = noteEntity.reference,
    content = noteEntity.content,
    createdBy = noteEntity.createdBy,
    createdAt = noteEntity.createdAt,
    createdAtPrison = noteEntity.createdAtPrison,
    lastUpdatedBy = noteEntity.updatedBy,
    lastUpdatedAt = noteEntity.updatedAt,
    lastUpdatedAtPrison = noteEntity.updatedAtPrison,
    noteType = toNoteType(noteEntity.noteType),
    entityType = toEntityType(noteEntity.entityType),
    entityReference = noteEntity.entityReference,
  )

  fun fromDomainNoteTypeToEntityNoteType(noteType: DomainNoteType): EntityNoteType = when (noteType) {
    DomainNoteType.GOAL -> EntityNoteType.GOAL
    DomainNoteType.GOAL_ARCHIVAL -> EntityNoteType.GOAL_ARCHIVAL
    DomainNoteType.GOAL_COMPLETION -> EntityNoteType.GOAL_COMPLETION
    DomainNoteType.REVIEW -> EntityNoteType.REVIEW
    DomainNoteType.INDUCTION -> EntityNoteType.INDUCTION
  }

  fun fromDomainEntityTypeToEntityEntityType(entityType: DomainEntityType): EntityEntityType = when (entityType) {
    DomainEntityType.GOAL -> EntityEntityType.GOAL
    DomainEntityType.REVIEW -> EntityEntityType.REVIEW
    DomainEntityType.INDUCTION -> EntityEntityType.INDUCTION
  }

  private fun toNoteType(noteType: EntityNoteType): DomainNoteType = when (noteType) {
    EntityNoteType.GOAL -> DomainNoteType.GOAL
    EntityNoteType.GOAL_ARCHIVAL -> DomainNoteType.GOAL_ARCHIVAL
    EntityNoteType.GOAL_COMPLETION -> DomainNoteType.GOAL_COMPLETION
    EntityNoteType.REVIEW -> DomainNoteType.REVIEW
    EntityNoteType.INDUCTION -> DomainNoteType.INDUCTION
  }

  private fun toEntityType(entityType: EntityEntityType): DomainEntityType = when (entityType) {
    EntityEntityType.GOAL -> DomainEntityType.GOAL
    EntityEntityType.REVIEW -> DomainEntityType.REVIEW
    EntityEntityType.INDUCTION -> DomainEntityType.INDUCTION
  }
}
