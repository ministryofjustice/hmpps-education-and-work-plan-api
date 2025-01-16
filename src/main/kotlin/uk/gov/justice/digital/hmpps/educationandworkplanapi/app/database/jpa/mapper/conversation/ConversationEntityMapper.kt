package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.conversation

import org.springframework.data.domain.Page
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.PagedResult
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.Conversation
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.CreateConversationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.UpdateConversationDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.ConversationEntity
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.ConversationType as ConversationTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.ConversationType as ConversationTypeEntity

@Component
class ConversationEntityMapper(
  private val conversationNoteEntityMapper: ConversationNoteEntityMapper,
) {

  /**
   * Maps the supplied [CreateConversationDto] into a new un-persisted [ConversationEntity].
   * A new reference number is generated and mapped. The JPA managed fields are not mapped.
   * This method is suitable for creating a new [ConversationEntity] to be subsequently persisted to the database.
   */
  fun fromCreateDtoToEntity(createConversationDto: CreateConversationDto): ConversationEntity =
    with(createConversationDto) {
      ConversationEntity(
        reference = UUID.randomUUID(),
        note = conversationNoteEntityMapper.fromCreateDtoToEntity(note),
        type = toType(type),
        prisonNumber = prisonNumber,
      )
    }

  /**
   * Maps the supplied [ConversationEntity] into the domain [Conversation].
   */
  fun fromEntityToDomain(conversationEntity: ConversationEntity): Conversation =
    with(conversationEntity) {
      Conversation(
        reference = reference,
        prisonNumber = prisonNumber,
        type = toType(type),
        note = conversationNoteEntityMapper.fromEntityToDomain(note),
      )
    }

  /**
   * Maps the supplied [Page] into the domain [PagedResult].
   */
  fun fromPagedEntitiesToDomain(pagedConversations: Page<ConversationEntity>): PagedResult<Conversation> =
    with(pagedConversations) {
      PagedResult(
        totalElements = totalElements.toInt(),
        totalPages = totalPages,
        pageNumber = number,
        pageSize = size,
        content = content.map { fromEntityToDomain(it) },
      )
    }

  fun updateEntityFromDto(conversationEntity: ConversationEntity, updateConversationDto: UpdateConversationDto) =
    with(conversationEntity) {
      note.content = updateConversationDto.noteContent
      note.updatedAtPrison = updateConversationDto.prisonId
    }

  private fun toType(type: ConversationTypeEntity): ConversationTypeDomain =
    when (type) {
      ConversationTypeEntity.GENERAL -> ConversationTypeDomain.GENERAL
      ConversationTypeEntity.REVIEW -> ConversationTypeDomain.REVIEW
      ConversationTypeEntity.INDUCTION -> ConversationTypeDomain.INDUCTION
    }

  private fun toType(type: ConversationTypeDomain): ConversationTypeEntity =
    when (type) {
      ConversationTypeDomain.GENERAL -> ConversationTypeEntity.GENERAL
      ConversationTypeDomain.REVIEW -> ConversationTypeEntity.REVIEW
      ConversationTypeDomain.INDUCTION -> ConversationTypeEntity.INDUCTION
    }
}
