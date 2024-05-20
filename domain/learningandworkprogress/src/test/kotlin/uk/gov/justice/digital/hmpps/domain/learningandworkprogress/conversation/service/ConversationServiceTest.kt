package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.ConversationNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.aValidConversation
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.aValidConversationNote
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.aValidCreateConversationDto
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ConversationServiceTest {

  @InjectMocks
  private lateinit var service: ConversationService

  @Mock
  private lateinit var persistenceAdapter: ConversationPersistenceAdapter

  @Mock
  private lateinit var conversationEventService: ConversationEventService

  @Test
  fun `should get conversation`() {
    // Given
    val conversationReference = UUID.randomUUID()
    val conversation = aValidConversation(reference = conversationReference)

    given(persistenceAdapter.getConversation(any())).willReturn(conversation)

    // When
    val actual = service.getConversation(conversationReference)

    // Then
    assertThat(actual).isEqualTo(conversation)
    verify(persistenceAdapter).getConversation(conversationReference)
  }

  @Test
  fun `should not get conversation given conversation does not exist`() {
    // Given
    val conversationReference = UUID.randomUUID()

    given(persistenceAdapter.getConversation(any())).willReturn(null)

    // When
    val exception = catchThrowableOfType(
      { service.getConversation(conversationReference) },
      ConversationNotFoundException::class.java,
    )

    // Then
    assertThat(exception.conversationReference).isEqualTo(conversationReference)
    assertThat(exception).hasMessage("Conversation with reference [$conversationReference] not found")
    verify(persistenceAdapter).getConversation(conversationReference)
  }

  @Test
  fun `should get prisoner conversations`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    val conversations = listOf(
      aValidConversation(prisonNumber = prisonNumber, note = aValidConversationNote(content = "Notes from chat 1")),
      aValidConversation(prisonNumber = prisonNumber, note = aValidConversationNote(content = "Notes from chat 2")),
      aValidConversation(prisonNumber = prisonNumber, note = aValidConversationNote(content = "Notes from chat 3")),
    )
    given(persistenceAdapter.getConversations(any())).willReturn(conversations)

    // When
    val actual = service.getPrisonerConversations(prisonNumber)

    // Then
    assertThat(actual).isEqualTo(conversations)
    verify(persistenceAdapter).getConversations(prisonNumber)
  }

  @Test
  fun `should get empty list of prisoner conversations given prisoner has no conversations`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    given(persistenceAdapter.getConversations(any())).willReturn(emptyList())

    // When
    val actual = service.getPrisonerConversations(prisonNumber)

    // Then
    assertThat(actual).isEmpty()
    verify(persistenceAdapter).getConversations(prisonNumber)
  }

  @Test
  fun `should create conversation`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    val conversation = aValidConversation(prisonNumber = prisonNumber)
    given(persistenceAdapter.createConversation(any())).willReturn(conversation)

    val createConversationDto = aValidCreateConversationDto(prisonNumber = prisonNumber)

    // When
    val actual = service.createConversation(createConversationDto)

    // Then
    assertThat(actual).isEqualTo(conversation)
    verify(persistenceAdapter).createConversation(createConversationDto)
    verify(conversationEventService).conversationCreated(conversation)
  }
}
