package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.conversation

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.Conversation
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.CreateConversationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.UpdateConversationDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ConversationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateConversationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateConversationRequest
import java.time.Instant
import java.util.UUID

@Mapper(
  uses = [
    InstantMapper::class,
  ],
  imports = [
    Instant::class,
  ],
)
interface ConversationsResourceMapper {
  @Mapping(source = "request.prisonId", target = "note.prisonId")
  @Mapping(source = "request.note", target = "note.content")
  fun toCreateConversationDto(request: CreateConversationRequest, prisonNumber: String): CreateConversationDto

  @Mapping(source = "request.note", target = "noteContent")
  fun toUpdateConversationDto(request: UpdateConversationRequest, reference: UUID): UpdateConversationDto

  @Mapping(source = "note.content", target = "note")
  @Mapping(source = "note.createdBy", target = "createdBy")
  @Mapping(source = "note.createdByDisplayName", target = "createdByDisplayName")
  @Mapping(source = "note.createdAt", target = "createdAt")
  @Mapping(source = "note.createdAtPrison", target = "createdAtPrison")
  @Mapping(source = "note.lastUpdatedBy", target = "updatedBy")
  @Mapping(source = "note.lastUpdatedByDisplayName", target = "updatedByDisplayName")
  @Mapping(source = "note.lastUpdatedAt", target = "updatedAt")
  @Mapping(source = "note.lastUpdatedAtPrison", target = "updatedAtPrison")
  fun fromDomainToModel(conversation: Conversation): ConversationResponse
}
