package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.Conversation
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.CreateConversationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.UpdateConversationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.service.ConversationPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.conversation.ConversationEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ConversationRepository
import java.util.UUID

@Component
class JpaConversationPersistenceAdapter(
  private val conversationRepository: ConversationRepository,
  private val conversationEntityMapper: ConversationEntityMapper,
) : ConversationPersistenceAdapter {

  @Transactional
  override fun createConversation(createConversationDto: CreateConversationDto): Conversation {
    val persistedEntity = conversationRepository.saveAndFlush(conversationEntityMapper.fromCreateDtoToEntity(createConversationDto))
    return conversationEntityMapper.fromEntityToDomain(persistedEntity)
  }

  @Transactional
  override fun updateConversation(updateConversationDto: UpdateConversationDto): Conversation? {
    val conversationEntity = conversationRepository.findByReference(updateConversationDto.reference)
    return if (conversationEntity != null) {
      conversationEntityMapper.updateEntityFromDto(conversationEntity, updateConversationDto)
      val persistedEntity = conversationRepository.saveAndFlush(conversationEntity)
      conversationEntityMapper.fromEntityToDomain(persistedEntity)
    } else {
      null
    }
  }

  @Transactional(readOnly = true)
  override fun getConversations(prisonNumber: String): List<Conversation> =
    conversationRepository.findAllByPrisonNumber(prisonNumber).map {
      conversationEntityMapper.fromEntityToDomain(it)
    }

  @Transactional(readOnly = true)
  override fun getConversation(conversationReference: UUID): Conversation? =
    conversationRepository.findByReference(conversationReference)?.let {
      conversationEntityMapper.fromEntityToDomain(it)
    }
}