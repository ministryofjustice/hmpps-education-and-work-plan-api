package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.conversation

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.Conversation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ConversationResponse

@Component
class NewConversationsResourceMapper(private val instantMapper: InstantMapper, private val userService: ManageUserService) {
  fun fromDomainToModel(conversation: Conversation): ConversationResponse {
    return ConversationResponse(
      reference = conversation.reference,
      prisonNumber = conversation.prisonNumber,
      note = conversation.note.content,
      createdBy = conversation.note.createdBy!!,
      createdByDisplayName = userService.getUserDetails(conversation.note.createdBy!!)!!.name,
      createdAt = instantMapper.toOffsetDateTime(conversation.note.createdAt)!!,
      createdAtPrison = conversation.note.createdAtPrison,
      updatedBy = conversation.note.lastUpdatedBy!!,
      updatedByDisplayName = userService.getUserDetails(conversation.note.lastUpdatedBy!!)!!.name,
      updatedAt = instantMapper.toOffsetDateTime(conversation.note.lastUpdatedAt)!!,
      updatedAtPrison = conversation.note.lastUpdatedAtPrison,
    )
  }
}
