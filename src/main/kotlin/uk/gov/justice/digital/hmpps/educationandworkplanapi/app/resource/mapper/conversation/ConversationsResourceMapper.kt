package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.conversation

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.CreateConversationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.UpdateConversationDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateConversationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateConversationRequest
import java.util.UUID

@Mapper()
interface ConversationsResourceMapper {
  @Mapping(source = "request.prisonId", target = "note.prisonId")
  @Mapping(source = "request.note", target = "note.content")
  fun toCreateConversationDto(request: CreateConversationRequest, prisonNumber: String): CreateConversationDto

  @Mapping(source = "request.note", target = "noteContent")
  fun toUpdateConversationDto(request: UpdateConversationRequest, reference: UUID): UpdateConversationDto
}
