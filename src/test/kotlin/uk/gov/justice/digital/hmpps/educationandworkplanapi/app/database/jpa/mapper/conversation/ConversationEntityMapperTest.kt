package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.conversation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.aValidConversation
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.aValidConversationNote
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.aValidCreateConversationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.aValidCreateConversationNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.aValidUpdateConversationDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.aValidConversationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.aValidConversationNoteEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.assertThat
import java.time.Instant
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.ConversationType as DomainType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.ConversationType as EntityType

@ExtendWith(MockitoExtension::class)
class ConversationEntityMapperTest {

  @InjectMocks
  private lateinit var mapper: ConversationEntityMapperImpl

  @Mock
  private lateinit var conversationNoteMapper: ConversationNoteEntityMapper

  @Test
  fun `should map from create DTO to entity`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    val createConversationNoteDto = aValidCreateConversationNoteDto(
      prisonId = "BXI",
      content = "Chris engaged well during our meeting and has made some good progress",
    )

    val createConversationDto = aValidCreateConversationDto(
      prisonNumber = prisonNumber,
      type = DomainType.REVIEW,
      note = createConversationNoteDto,
    )

    val expectedConversationNoteEntity = aValidConversationNoteEntity(
      content = "Chris engaged well during our meeting and has made some good progress",
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
      // JPA managed fields - expect these all to be null, implying a new db entity
      id = null,
      createdAt = null,
      createdBy = null,
      createdByDisplayName = null,
      updatedAt = null,
      updatedBy = null,
      updatedByDisplayName = null,
    )
    given(conversationNoteMapper.fromCreateDtoToEntity(any())).willReturn(expectedConversationNoteEntity)

    val expected = aValidConversationEntity(
      prisonNumber = prisonNumber,
      conversationNote = expectedConversationNoteEntity,
      type = EntityType.REVIEW,
      // JPA managed fields - expect these all to be null, implying a new db entity
      id = null,
      createdAt = null,
      createdBy = null,
      updatedAt = null,
      updatedBy = null,
    )

    // When
    val actual = mapper.fromCreateDtoToEntity(createConversationDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .hasAReference()
      .usingRecursiveComparison()
      .ignoringFields("reference")
      .isEqualTo(expected)
    verify(conversationNoteMapper).fromCreateDtoToEntity(createConversationNoteDto)
  }

  @Test
  fun `should map from entity to domain`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createdAt = Instant.now()
    val updatedAt = Instant.now()

    val conversationNoteEntity = aValidConversationNoteEntity(
      content = "Chris engaged well during our meeting and has made some good progress",
      createdAtPrison = "BXI",
      updatedAtPrison = "MDI",
      createdAt = createdAt,
      createdBy = "asmith_gen",
      createdByDisplayName = "Alex Smith",
      updatedAt = updatedAt,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "Barry Jones",
    )
    val entity = aValidConversationEntity(
      prisonNumber = prisonNumber,
      type = EntityType.REVIEW,
      conversationNote = conversationNoteEntity,
      createdAt = createdAt,
      createdBy = "asmith_gen",
      updatedAt = updatedAt,
      updatedBy = "bjones_gen",
    )

    val expectedConversationNote = aValidConversationNote(
      reference = entity.note!!.reference!!,
      content = "Chris engaged well during our meeting and has made some good progress",
      createdAtPrison = "BXI",
      lastUpdatedAtPrison = "MDI",
      createdAt = entity.note!!.createdAt,
      createdBy = "asmith_gen",
      createdByDisplayName = "Alex Smith",
      lastUpdatedAt = entity.note!!.updatedAt,
      lastUpdatedBy = "bjones_gen",
      lastUpdatedByDisplayName = "Barry Jones",
    )
    given(conversationNoteMapper.fromEntityToDomain(any())).willReturn(expectedConversationNote)

    val expected = aValidConversation(
      reference = entity.reference!!,
      type = DomainType.REVIEW,
      note = expectedConversationNote,
    )

    // When
    val actual = mapper.fromEntityToDomain(entity)

    // Then
    assertThat(actual).isEqualTo(expected)
    verify(conversationNoteMapper).fromEntityToDomain(conversationNoteEntity)
  }

  @Test
  fun `should update conversation entity from data in UpdateConversationDto`() {
    // Given
    val conversationReference = UUID.randomUUID()

    val conversationEntity = aValidConversationEntity(
      reference = conversationReference,
      conversationNote = aValidConversationNoteEntity(
        content = "The original note content",
      ),
    )

    val updateConversationDto = aValidUpdateConversationDto(
      reference = conversationReference,
      noteContent = "The updated note content",
    )

    val expectedEntity = aValidConversationEntity(
      reference = conversationReference,
      conversationNote = aValidConversationNoteEntity(
        content = "The updated note content",
      ),
    )

    // When
    mapper.updateEntityFromDto(conversationEntity, updateConversationDto)

    // Then
    assertThat(conversationEntity).isEqualToIgnoringJpaManagedFields(expectedEntity)
  }
}
