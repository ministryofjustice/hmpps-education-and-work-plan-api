package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.conversation

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.ConversationNote
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.CreateConversationNoteDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Step
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.ConversationNoteEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFieldsIncludingDisplayNameFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import java.util.UUID

@Mapper(
  imports = [UUID::class],
)
interface ConversationNoteEntityMapper {

  /**
   * Maps the supplied [CreateConversationNoteDto] into a new un-persisted [ConversationNoteEntity].
   * A new reference number is generated and mapped. The JPA managed fields are not mapped.
   * This method is suitable for creating a new [ConversationNoteEntity] to be subsequently persisted to the database.
   */
  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  fun fromCreateDtoToEntity(createConversationNoteDto: CreateConversationNoteDto): ConversationNoteEntity

  /**
   * Maps the supplied [ConversationNoteEntity] into the domain [Step].
   */
  @Mapping(target = "lastUpdatedBy", source = "updatedBy")
  @Mapping(target = "lastUpdatedByDisplayName", source = "updatedByDisplayName")
  @Mapping(target = "lastUpdatedAt", source = "updatedAt")
  @Mapping(target = "lastUpdatedAtPrison", source = "updatedAtPrison")
  fun fromEntityToDomain(conversationNoteEntity: ConversationNoteEntity): ConversationNote
}
