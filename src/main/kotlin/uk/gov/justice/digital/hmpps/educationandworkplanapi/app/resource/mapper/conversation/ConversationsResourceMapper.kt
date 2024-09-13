package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.conversation

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.PagedResult
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.Conversation
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.CreateConversationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.CreateConversationNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.UpdateConversationDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ConversationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ConversationType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ConversationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateConversationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateConversationRequest
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.ConversationType as DomainConversationType

@Component
class ConversationsResourceMapper(
  private val instantMapper: InstantMapper,
  private val userService: ManageUserService,
) {
  fun fromDomainToModel(conversation: Conversation) =
    ConversationResponse(
      reference = conversation.reference,
      prisonNumber = conversation.prisonNumber,
      note = conversation.note.content,
      createdBy = conversation.note.createdBy!!,
      createdByDisplayName = conversation.note.createdBy?.let {
        userService.getUserDetails(it)?.name
      } ?: "Unknown",
      createdAt = instantMapper.toOffsetDateTime(conversation.note.createdAt)!!,
      createdAtPrison = conversation.note.createdAtPrison,
      updatedBy = conversation.note.lastUpdatedBy!!,
      updatedByDisplayName = conversation.note.lastUpdatedBy?.let {
        userService.getUserDetails(it)?.name
      } ?: "Unknown",
      updatedAt = instantMapper.toOffsetDateTime(conversation.note.lastUpdatedAt)!!,
      updatedAtPrison = conversation.note.lastUpdatedAtPrison,
    )

  fun toCreateConversationDto(request: CreateConversationRequest, prisonNumber: String) =
    CreateConversationDto(
      prisonNumber = prisonNumber,
      type = conversationTypeToConversationType(request.type),
      note = CreateConversationNoteDto(request.prisonId, request.note),
    )

  fun toUpdateConversationDto(request: UpdateConversationRequest, reference: UUID) =
    UpdateConversationDto(
      reference = reference,
      noteContent = request.note,
      prisonId = request.prisonId,
    )

  fun fromPagedDomainToModel(pagedConversations: PagedResult<Conversation>) =
    ConversationsResponse(
      totalElements = pagedConversations.totalElements,
      totalPages = pagedConversations.totalPages,
      pageNumber = pagedConversations.pageNumber,
      pageSize = pagedConversations.pageSize,
      content = conversationListToConversationResponseList(pagedConversations.content),
    )

  protected fun conversationListToConversationResponseList(list: List<Conversation>): List<ConversationResponse> {
    return list.map { fromDomainToModel(it) }
  }

  protected fun conversationTypeToConversationType(conversationType: ConversationType) =
    when (conversationType) {
      ConversationType.INDUCTION -> DomainConversationType.INDUCTION
      ConversationType.GENERAL -> DomainConversationType.GENERAL
      ConversationType.REVIEW -> DomainConversationType.REVIEW
    }
}
