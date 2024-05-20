package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.conversation

import org.mapstruct.Mapper
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.Conversation
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.CreateConversationDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.ConversationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import java.util.UUID

@Mapper(
  imports = [UUID::class],
  uses = [ConversationNoteEntityMapper::class],
)
interface ConversationEntityMapper {

  /**
   * Maps the supplied [CreateConversationDto] into a new un-persisted [ConversationEntity].
   * A new reference number is generated and mapped. The JPA managed fields are not mapped.
   * This method is suitable for creating a new [ConversationEntity] to be subsequently persisted to the database.
   */
  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromCreateDtoToEntity(createConversationDto: CreateConversationDto): ConversationEntity

  /**
   * Maps the supplied [ConversationEntity] into the domain [Conversation].
   */
  fun fromEntityToDomain(conversationEntity: ConversationEntity): Conversation
}
