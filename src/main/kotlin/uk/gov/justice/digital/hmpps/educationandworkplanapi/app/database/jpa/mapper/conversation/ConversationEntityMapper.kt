package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.conversation

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.PagedResult
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.Conversation
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.CreateConversationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.UpdateConversationDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.ConversationEntity
import java.time.Instant
import java.util.Collections
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.ConversationType as ConversationTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.ConversationType as ConversationTypeEntity

@Mapper(
  imports = [UUID::class, Collections::class],
  uses = [ConversationNoteEntityMapper::class],
)
abstract class ConversationEntityMapper {

  @Autowired
  private lateinit var conversationNoteEntityMapper: ConversationNoteEntityMapper

  /**
   * Maps the supplied [CreateConversationDto] into a new un-persisted [ConversationEntity].
   * A new reference number is generated.
   * This method is suitable for creating a new [ConversationEntity] to be subsequently persisted to the database.
   */
  fun fromCreateDtoToEntity(createConversationDto: CreateConversationDto): ConversationEntity =
    with(createConversationDto) {
      val now = Instant.now()
      ConversationEntity(
        reference = UUID.randomUUID(),
        note = conversationNoteEntityMapper.fromCreateDtoToEntity(note, createdBy, createdByDisplayName),
        type = toConversationType(type),
        prisonNumber = prisonNumber,
        createdAt = now,
        createdBy = createdBy,
        updatedAt = now,
        updatedBy = createdBy,
      )
    }

  fun toConversationType(type: ConversationTypeDomain): ConversationTypeEntity =
    when (type) {
      ConversationTypeDomain.INDUCTION -> ConversationTypeEntity.INDUCTION
      ConversationTypeDomain.REVIEW -> ConversationTypeEntity.REVIEW
      ConversationTypeDomain.GENERAL -> ConversationTypeEntity.GENERAL
    }

  /**
   * Maps the supplied [ConversationEntity] into the domain [Conversation].
   */
  abstract fun fromEntityToDomain(conversationEntity: ConversationEntity): Conversation

  /**
   * Maps the supplied [Page] into the domain [PagedResult].
   */
  @Mapping(target = "pageNumber", source = "number")
  @Mapping(target = "pageSize", source = "size")
  @Mapping(target = "content", defaultExpression = "java(Collections.emptyList())")
  abstract fun fromPagedEntitiesToDomain(pagedConversations: Page<ConversationEntity>?): PagedResult<Conversation>

  fun updateEntityFromDto(conversationEntity: ConversationEntity, updateConversationDto: UpdateConversationDto) {
    conversationEntity.apply {
      val now = Instant.now()
      with(note!!) {
        content = updateConversationDto.noteContent
        updatedAtPrison = updateConversationDto.prisonId
        updatedAt = now
        updatedBy = updateConversationDto.updatedBy
        updatedByDisplayName = updateConversationDto.updatedByDisplayName
      }
      updatedAt = now
      updatedBy = updateConversationDto.updatedBy
    }
  }
}
