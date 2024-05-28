package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.conversation

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.CreateConversationDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateConversationRequest

@Mapper()
interface ConversationsResourceMapper {
  @Mapping(source = "request.prisonId", target = "note.prisonId")
  @Mapping(source = "request.note", target = "note.content")
  fun toCreateConversationDto(request: CreateConversationRequest, prisonNumber: String): CreateConversationDto
}
