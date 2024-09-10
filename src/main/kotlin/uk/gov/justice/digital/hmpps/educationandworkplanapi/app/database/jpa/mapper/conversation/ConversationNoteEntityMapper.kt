package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.conversation

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.ConversationNote
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.CreateConversationNoteDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Step
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.ConversationNoteEntity
import java.time.Instant
import java.util.UUID

@Mapper(
  imports = [UUID::class],
)
abstract class ConversationNoteEntityMapper {

  /**
   * Maps the supplied [CreateConversationNoteDto] into a new un-persisted [ConversationNoteEntity].
   * A new reference number is generated.
   * This method is suitable for creating a new [ConversationNoteEntity] to be subsequently persisted to the database.
   */
  fun fromCreateDtoToEntity(createConversationNoteDto: CreateConversationNoteDto, createdBy: String, createdByDisplayName: String): ConversationNoteEntity =
    with(createConversationNoteDto) {
      val now = Instant.now()
      ConversationNoteEntity(
        reference = UUID.randomUUID(),
        content = content,
        createdAt = now,
        createdAtPrison = prisonId,
        createdBy = createdBy,
        createdByDisplayName = createdByDisplayName,
        updatedAt = now,
        updatedAtPrison = prisonId,
        updatedBy = createdBy,
        updatedByDisplayName = createdByDisplayName,
      )
    }

  /**
   * Maps the supplied [ConversationNoteEntity] into the domain [Step].
   */
  @Mapping(target = "lastUpdatedBy", source = "updatedBy")
  @Mapping(target = "lastUpdatedByDisplayName", source = "updatedByDisplayName")
  @Mapping(target = "lastUpdatedAt", source = "updatedAt")
  @Mapping(target = "lastUpdatedAtPrison", source = "updatedAtPrison")
  abstract fun fromEntityToDomain(conversationNoteEntity: ConversationNoteEntity): ConversationNote
}
