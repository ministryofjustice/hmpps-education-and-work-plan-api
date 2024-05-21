package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.aValidConversation
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.aValidCreateConversationDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.ConversationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.aValidConversationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.conversation.ConversationEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ConversationRepository
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class JpaConversationPersistenceAdapterTest {

  @InjectMocks
  private lateinit var persistenceAdapter: JpaConversationPersistenceAdapter

  @Mock
  private lateinit var conversationRepository: ConversationRepository

  @Mock
  private lateinit var conversationEntityMapper: ConversationEntityMapper

  @Test
  fun `should create conversation`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    val conversationEntityBeforePersisted = aValidConversationEntity(
      prisonNumber = prisonNumber,
      // JPA fields null as entity not yet persisted
      id = null,
      createdAt = null,
      createdBy = null,
      updatedAt = null,
      updatedBy = null,
    )
    given(conversationEntityMapper.fromCreateDtoToEntity(any())).willReturn(conversationEntityBeforePersisted)

    val conversationEntityAfterPersisted = aValidConversationEntity(prisonNumber = prisonNumber)
    given(conversationRepository.saveAndFlush(any<ConversationEntity>())).willReturn(conversationEntityAfterPersisted)

    val expectedConversation = aValidConversation(prisonNumber = prisonNumber)
    given(conversationEntityMapper.fromEntityToDomain(any())).willReturn(expectedConversation)

    val createConversationDto = aValidCreateConversationDto(prisonNumber = prisonNumber)

    // When
    val actual = persistenceAdapter.createConversation(createConversationDto)

    // Then
    assertThat(actual).isEqualTo(expectedConversation)
    verify(conversationEntityMapper).fromCreateDtoToEntity(createConversationDto)
    verify(conversationRepository).saveAndFlush(conversationEntityBeforePersisted)
    verify(conversationEntityMapper).fromEntityToDomain(conversationEntityAfterPersisted)
  }

  @Test
  fun `should get conversation`() {
    // Given
    val conversationReference = UUID.randomUUID()

    val conversationEntity = aValidConversationEntity(reference = conversationReference)
    given(conversationRepository.findByReference(any())).willReturn(conversationEntity)

    val expectedConversation = aValidConversation(reference = conversationReference)
    given(conversationEntityMapper.fromEntityToDomain(any())).willReturn(expectedConversation)

    // When
    val actual = persistenceAdapter.getConversation(conversationReference)

    // Then
    assertThat(actual).isEqualTo(expectedConversation)
    verify(conversationRepository).findByReference(conversationReference)
    verify(conversationEntityMapper).fromEntityToDomain(conversationEntity)
  }

  @Test
  fun `should not get conversation given conversation does not exist`() {
    // Given
    val conversationReference = UUID.randomUUID()

    given(conversationRepository.findByReference(any())).willReturn(null)

    // When
    val actual = persistenceAdapter.getConversation(conversationReference)

    // Then
    assertThat(actual).isNull()
    verify(conversationRepository).findByReference(conversationReference)
    verify(conversationEntityMapper, never()).fromEntityToDomain(any())
  }

  @Test
  fun `should get conversations`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    val conversationEntity1 = aValidConversationEntity(prisonNumber = prisonNumber)
    val conversationEntity2 = aValidConversationEntity(prisonNumber = prisonNumber)
    given(conversationRepository.findAllByPrisonNumber(any())).willReturn(listOf(conversationEntity1, conversationEntity2))

    val expectedConversationEntity1 = aValidConversation(prisonNumber = prisonNumber)
    val expectedConversationEntity2 = aValidConversation(prisonNumber = prisonNumber)
    given(conversationEntityMapper.fromEntityToDomain(any())).willReturn(expectedConversationEntity1, expectedConversationEntity2)

    // When
    val actual = persistenceAdapter.getConversations(prisonNumber)

    // Then
    assertThat(actual).containsExactlyInAnyOrder(expectedConversationEntity1, expectedConversationEntity2)
    verify(conversationRepository).findAllByPrisonNumber(prisonNumber)
    verify(conversationEntityMapper).fromEntityToDomain(conversationEntity1)
    verify(conversationEntityMapper).fromEntityToDomain(conversationEntity2)
  }

  @Test
  fun `should get empty list of conversations given prisoner has no conversations`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    given(conversationRepository.findAllByPrisonNumber(any())).willReturn(emptyList())

    // When
    val actual = persistenceAdapter.getConversations(prisonNumber)

    // Then
    assertThat(actual).isEmpty()
    verify(conversationRepository).findAllByPrisonNumber(prisonNumber)
    verify(conversationEntityMapper, never()).fromEntityToDomain(any())
  }
}
