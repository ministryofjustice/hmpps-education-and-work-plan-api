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
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.aValidPagedResult
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.Conversation
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.PrisonerConversationNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.aValidConversation
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.aValidConversationNote
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.aValidCreateConversationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.aValidUpdateConversationDto
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
    val prisonNumber = aValidPrisonNumber()
    val conversation = aValidConversation(
      reference = conversationReference,
      prisonNumber = prisonNumber,
    )

    given(persistenceAdapter.getConversation(any(), any())).willReturn(conversation)

    // When
    val actual = service.getConversation(conversationReference, prisonNumber)

    // Then
    assertThat(actual).isEqualTo(conversation)
    verify(persistenceAdapter).getConversation(conversationReference, prisonNumber)
  }

  @Test
  fun `should not get conversation given conversation does not exist`() {
    // Given
    val conversationReference = UUID.randomUUID()
    val prisonNumber = aValidPrisonNumber()

    given(persistenceAdapter.getConversation(any(), any())).willReturn(null)

    // When
    val exception = catchThrowableOfType(PrisonerConversationNotFoundException::class.java) {
      service.getConversation(conversationReference, prisonNumber)
    }

    // Then
    assertThat(exception.conversationReference).isEqualTo(conversationReference)
    assertThat(exception).hasMessage("Conversation with reference [$conversationReference] for prisoner [$prisonNumber] not found")
    verify(persistenceAdapter).getConversation(conversationReference, prisonNumber)
  }

  @Test
  fun `should get a page of prisoner conversations`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    val conversations = listOf(
      aValidConversation(prisonNumber = prisonNumber, note = aValidConversationNote(content = "Notes from chat 1")),
      aValidConversation(prisonNumber = prisonNumber, note = aValidConversationNote(content = "Notes from chat 2")),
      aValidConversation(prisonNumber = prisonNumber, note = aValidConversationNote(content = "Notes from chat 3")),
    )
    val conversationsPage = aValidPagedResult(
      content = conversations,
    )
    given(persistenceAdapter.getPagedConversations(any(), any(), any())).willReturn(conversationsPage)

    // When
    val actual = service.getPrisonerConversations(prisonNumber, 0, 20)

    // Then
    with(actual) {
      assertThat(totalElements).isEqualTo(3)
      assertThat(content).isEqualTo(conversations)
    }
    verify(persistenceAdapter).getPagedConversations(prisonNumber, 0, 20)
  }

  @Test
  fun `should get empty page of prisoner conversations given prisoner has no conversations`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    val emptyPagedResult = aValidPagedResult(
      content = emptyList<Conversation>(),
    )
    given(persistenceAdapter.getPagedConversations(any(), any(), any())).willReturn(emptyPagedResult)

    // When
    val actual = service.getPrisonerConversations(prisonNumber, 0, 20)

    // Then
    with(actual) {
      assertThat(pageNumber).isEqualTo(0)
      assertThat(pageSize).isEqualTo(20)
      assertThat(totalElements).isEqualTo(0)
      assertThat(totalPages).isEqualTo(1)
      assertThat(content).hasSize(0)
    }
    verify(persistenceAdapter).getPagedConversations(prisonNumber, 0, 20)
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

  @Test
  fun `should update conversation`() {
    // Given
    val conversationReference = UUID.randomUUID()
    val prisonNumber = aValidPrisonNumber()

    val updateConversationDto = aValidUpdateConversationDto(
      reference = conversationReference,
      noteContent = "Chris spoke positively about future work during our meeting and has made some good progress",
    )

    val updatedConversation = aValidConversation(
      reference = conversationReference,
      prisonNumber = prisonNumber,
      note = aValidConversationNote(
        content = "Chris spoke positively about future work during our meeting and has made some good progress",
      ),
    )
    given(persistenceAdapter.updateConversation(any(), any())).willReturn(updatedConversation)

    // When
    val actual = service.updateConversation(updateConversationDto, prisonNumber)

    // Then
    assertThat(actual).isEqualTo(updatedConversation)
    verify(persistenceAdapter).updateConversation(updateConversationDto, prisonNumber)
  }

  @Test
  fun `should not update conversation given conversation does not exist`() {
    // Given
    val conversationReference = UUID.randomUUID()
    val prisonNumber = aValidPrisonNumber()

    val updateConversationDto = aValidUpdateConversationDto(
      reference = conversationReference,
      noteContent = "Chris spoke positively about future work during our meeting and has made some good progress",
    )

    given(persistenceAdapter.updateConversation(any(), any())).willReturn(null)

    // When
    val exception = catchThrowableOfType(PrisonerConversationNotFoundException::class.java) {
      service.updateConversation(
        updateConversationDto,
        prisonNumber,
      )
    }

    // Then
    assertThat(exception.conversationReference).isEqualTo(conversationReference)
    assertThat(exception).hasMessage("Conversation with reference [$conversationReference] for prisoner [$prisonNumber] not found")
    verify(persistenceAdapter).updateConversation(updateConversationDto, prisonNumber)
  }
}
