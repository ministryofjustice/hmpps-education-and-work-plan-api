package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.note

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.CreateNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteEntity
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.EntityType as DomainEntityType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteType as DomainNoteType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.EntityType as EntityEntityType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteType as EntityNoteType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.NoteType as ResourceNoteType

object NoteMapper {

  fun toEntity(createNoteDto: CreateNoteDto): NoteEntity {
    return NoteEntity(
      reference = UUID.randomUUID(),
      prisonNumber = createNoteDto.prisonNumber,
      content = createNoteDto.content,
      noteType = toEntity(createNoteDto.noteType),
      entityType = toEntity(createNoteDto.entityType),
      entityReference = createNoteDto.entityReference,
      createdAtPrison = createNoteDto.createdAtPrison,
      updatedAtPrison = createNoteDto.lastUpdatedAtPrison,
    )
  }

  fun toModel(noteEntity: NoteEntity): NoteDto {
    return NoteDto(
      reference = noteEntity.reference!!,
      content = noteEntity.content.orEmpty(),
      createdBy = noteEntity.createdBy,
      createdAt = noteEntity.createdAt,
      createdAtPrison = noteEntity.createdAtPrison.orEmpty(),
      lastUpdatedBy = noteEntity.updatedBy,
      lastUpdatedAt = noteEntity.updatedAt,
      lastUpdatedAtPrison = noteEntity.updatedAtPrison.orEmpty(),
      noteType = toModel(noteEntity.noteType!!),
      entityType = toModel(noteEntity.entityType!!),
      entityReference = noteEntity.reference!!,
    )
  }

  fun toEntity(noteType: DomainNoteType) =
    when (noteType) {
      DomainNoteType.GOAL -> EntityNoteType.GOAL
      DomainNoteType.GOAL_ARCHIVAL -> EntityNoteType.GOAL_ARCHIVAL
      DomainNoteType.GOAL_COMPLETION -> EntityNoteType.GOAL_COMPLETION
    }

  fun toModel(noteType: EntityNoteType) =
    when (noteType) {
      EntityNoteType.GOAL -> DomainNoteType.GOAL
      EntityNoteType.GOAL_ARCHIVAL -> DomainNoteType.GOAL_ARCHIVAL
      EntityNoteType.GOAL_COMPLETION -> DomainNoteType.GOAL_COMPLETION
    }

  fun toResourceModel(noteType: DomainNoteType) =
    when (noteType) {
      DomainNoteType.GOAL -> ResourceNoteType.GOAL
      DomainNoteType.GOAL_ARCHIVAL -> ResourceNoteType.GOAL_ARCHIVAL
      DomainNoteType.GOAL_COMPLETION -> ResourceNoteType.GOAL_COMPLETION
    }

  fun toEntity(entityType: DomainEntityType) =
    when (entityType) {
      DomainEntityType.GOAL -> EntityEntityType.GOAL
    }

  fun toModel(entityType: EntityEntityType) =
    when (entityType) {
      EntityEntityType.GOAL -> DomainEntityType.GOAL
    }
}
