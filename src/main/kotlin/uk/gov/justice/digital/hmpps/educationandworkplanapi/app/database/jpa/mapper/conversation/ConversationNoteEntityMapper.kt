package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.conversation

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.ConversationNote
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.CreateConversationNoteDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.ConversationNoteEntity
import java.util.UUID

@Component
class ConversationNoteEntityMapper {

  /**
   * Maps the supplied [CreateConversationNoteDto] into a new un-persisted [ConversationNoteEntity].
   * A new reference number is generated and mapped. The JPA managed fields are not mapped.
   * This method is suitable for creating a new [ConversationNoteEntity] to be subsequently persisted to the database.
   */
  fun fromCreateDtoToEntity(createConversationNoteDto: CreateConversationNoteDto): ConversationNoteEntity =
    with(createConversationNoteDto) {
      ConversationNoteEntity(
        reference = UUID.randomUUID(),
        content = content,
        createdAtPrison = prisonId,
        updatedAtPrison = prisonId,
      )
    }

  /**
   * Maps the supplied [ConversationNoteEntity] into the domain [ConversationNote].
   */
  fun fromEntityToDomain(conversationNoteEntity: ConversationNoteEntity): ConversationNote =
    with(conversationNoteEntity) {
      ConversationNote(
        reference = reference,
        content = content,
        createdBy = createdBy!!,
        createdAt = createdAt!!,
        createdAtPrison = createdAtPrison,
        lastUpdatedBy = updatedBy!!,
        lastUpdatedAt = updatedAt!!,
        lastUpdatedAtPrison = updatedAtPrison,
      )
    }
}
