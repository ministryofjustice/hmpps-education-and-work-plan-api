package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.conversation

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.springframework.data.domain.Page
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.PagedResult
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.Conversation
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.CreateConversationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.UpdateConversationDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.ConversationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeReferenceField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import java.util.*

@Mapper(
  imports = [UUID::class, Collections::class],
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

  /**
   * Maps the supplied [Page] into the domain [PagedResult].
   */
  @Mapping(target = "pageNumber", source = "number")
  @Mapping(target = "pageSize", source = "size")
  @Mapping(target = "content", defaultExpression = "java(Collections.emptyList())")
  fun fromPagedEntitiesToDomain(pagedConversations: Page<ConversationEntity>?): PagedResult<Conversation>

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  @Mapping(target = "note.content", source = "noteContent")
  @Mapping(target = "note.updatedAtPrison", source = "prisonId")
  @Mapping(target = "type", ignore = true)
  @Mapping(target = "prisonNumber", ignore = true)
  fun updateEntityFromDto(@MappingTarget conversationEntity: ConversationEntity, updateConversationDto: UpdateConversationDto)
}
