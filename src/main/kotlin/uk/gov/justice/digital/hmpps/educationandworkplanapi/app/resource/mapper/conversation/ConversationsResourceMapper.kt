package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.conversation

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.PagedResult
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.Conversation
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.CreateConversationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.CreateConversationNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.UpdateConversationDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ConversationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ConversationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateConversationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateConversationRequest
import java.time.Instant
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.ConversationType as ConversationTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ConversationType as ConversationTypeApi

@Mapper(
  uses = [
    InstantMapper::class,
  ],
  imports = [
    Instant::class,
  ],
)
abstract class ConversationsResourceMapper {
  fun toCreateConversationDto(request: CreateConversationRequest, prisonNumber: String, createdBy: String, createdByDisplayName: String): CreateConversationDto =
    CreateConversationDto(
      prisonNumber = prisonNumber,
      type = toConversationType(request.type),
      note = CreateConversationNoteDto(
        prisonId = request.prisonId,
        content = request.note,
      ),
      createdBy = createdBy,
      createdByDisplayName = createdByDisplayName,
    )

  fun toConversationType(type: ConversationTypeApi): ConversationTypeDomain =
    when (type) {
      ConversationTypeApi.INDUCTION -> ConversationTypeDomain.INDUCTION
      ConversationTypeApi.REVIEW -> ConversationTypeDomain.REVIEW
      ConversationTypeApi.GENERAL -> ConversationTypeDomain.GENERAL
    }

  fun toUpdateConversationDto(request: UpdateConversationRequest, reference: UUID, updatedBy: String, updatedByDisplayName: String): UpdateConversationDto =
    UpdateConversationDto(
      reference = reference,
      prisonId = request.prisonId,
      noteContent = request.note,
      updatedBy = updatedBy,
      updatedByDisplayName = updatedByDisplayName,
    )

  @Mapping(source = "note.content", target = "note")
  @Mapping(source = "note.createdBy", target = "createdBy")
  @Mapping(source = "note.createdByDisplayName", target = "createdByDisplayName")
  @Mapping(source = "note.createdAt", target = "createdAt")
  @Mapping(source = "note.createdAtPrison", target = "createdAtPrison")
  @Mapping(source = "note.lastUpdatedBy", target = "updatedBy")
  @Mapping(source = "note.lastUpdatedByDisplayName", target = "updatedByDisplayName")
  @Mapping(source = "note.lastUpdatedAt", target = "updatedAt")
  @Mapping(source = "note.lastUpdatedAtPrison", target = "updatedAtPrison")
  abstract fun fromDomainToModel(conversation: Conversation): ConversationResponse

  abstract fun fromPagedDomainToModel(pagedConversations: PagedResult<Conversation>): ConversationsResponse
}
