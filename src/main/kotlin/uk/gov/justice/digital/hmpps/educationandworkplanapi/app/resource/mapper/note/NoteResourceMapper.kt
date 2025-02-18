package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.note

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.NoteResponse
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteType as DomainNoteType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.NoteType as ApiNoteType

@Component
class NoteResourceMapper(
  private val instantMapper: InstantMapper,
  private val userService: ManageUserService,
) {

  fun fromDomainToModel(note: NoteDto): NoteResponse = NoteResponse(
    reference = note.reference,
    content = note.content,
    type = toNoteType(note.noteType),
    createdBy = note.createdBy!!,
    createdByDisplayName = userService.getUserDetails(note.createdBy!!).name,
    createdAt = instantMapper.toOffsetDateTime(note.createdAt)!!,
    createdAtPrison = note.createdAtPrison,
    updatedBy = note.lastUpdatedBy!!,
    updatedByDisplayName = userService.getUserDetails(note.lastUpdatedBy!!).name,
    updatedAt = instantMapper.toOffsetDateTime(note.lastUpdatedAt)!!,
    updatedAtPrison = note.lastUpdatedAtPrison,
  )

  private fun toNoteType(noteType: DomainNoteType): ApiNoteType = when (noteType) {
    DomainNoteType.GOAL -> ApiNoteType.GOAL
    DomainNoteType.GOAL_ARCHIVAL -> ApiNoteType.GOAL_ARCHIVAL
    DomainNoteType.GOAL_COMPLETION -> ApiNoteType.GOAL_COMPLETION
    DomainNoteType.REVIEW -> ApiNoteType.REVIEW
    DomainNoteType.INDUCTION -> ApiNoteType.INDUCTION
  }
}
