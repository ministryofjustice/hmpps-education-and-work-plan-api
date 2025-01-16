package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.conversation

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.aValidPagedResult
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.aValidConversation
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.aValidConversationNote
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.UserDetailsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.conversation.aValidConversationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.conversation.assertThat
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class ConversationResourceMapperTest {
  @InjectMocks
  private lateinit var mapper: ConversationsResourceMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Mock
  private lateinit var userService: ManageUserService

  @Test
  fun `should map from domain to model`() {
    // Given
    val reference = UUID.randomUUID()
    val prisonNumber = aValidPrisonNumber()
    val noteContent = "Pay close attention to John's behaviour."
    val createUsername = "auser_gen"
    val createDisplayName = "Albert User"
    val createPrison = "BXI"
    val updateUsername = "buser_gen"
    val updateDisplayName = "Bernie User"
    val updatePrison = "MDI"
    val expectedDateTime = OffsetDateTime.now()
    val conversation = aValidConversation(
      reference = reference,
      prisonNumber = prisonNumber,
      note = aValidConversationNote(
        content = noteContent,
        createdBy = createUsername,
        createdAt = Instant.now(),
        createdAtPrison = createPrison,
        lastUpdatedBy = updateUsername,
        lastUpdatedAt = Instant.now(),
        lastUpdatedAtPrison = updatePrison,
      ),
    )
    val expectedConversation = aValidConversationResponse(
      reference = conversation.reference,
      prisonNumber = conversation.prisonNumber,
      note = noteContent,
      createdBy = createUsername,
      createdByDisplayName = createDisplayName,
      createdAt = expectedDateTime,
      updatedBy = updateUsername,
      updatedByDisplayName = updateDisplayName,
      updatedAt = expectedDateTime,
      updatedAtPrison = updatePrison,
    )
    given(instantMapper.toOffsetDateTime(any())).willReturn(expectedDateTime)

    given(userService.getUserDetails(any())).willReturn(
      UserDetailsDto("auser_gen", true, "Albert User"),
      UserDetailsDto("buser_gen", true, "Bernie User"),
    )

    // When
    val actual = mapper.fromDomainToModel(conversation)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expectedConversation)
    verify(userService).getUserDetails("auser_gen")
    verify(userService).getUserDetails("buser_gen")
  }

  @Test
  fun `should map from paged domain to model`() {
    // Given
    val page = 0
    val pageSize = 10
    val conversation1 = aValidConversation(
      note = aValidConversationNote(
        content = "Pay attention to Peter's behaviour.",
      ),
    )
    val conversation2 = aValidConversation(
      note = aValidConversationNote(
        content = "Peter is progressing well.",
      ),
    )
    val conversation3 = aValidConversation(
      note = aValidConversationNote(
        content = "Peter has complete his goal.",
      ),
    )

    val pagedConversations = aValidPagedResult(
      content = listOf(conversation1, conversation2, conversation3),
      page = page,
      pageSize = pageSize,
    )

    val expectedDateTime = OffsetDateTime.now()
    given(instantMapper.toOffsetDateTime(any())).willReturn(expectedDateTime)

    given(userService.getUserDetails(any())).willReturn(
      UserDetailsDto("asmith_gen", true, "Alex Smith"),
      UserDetailsDto("bjones_gen", true, "Barry Jones"),
    )

    // When
    val actual = mapper.fromPagedDomainToModel(pagedConversations)

    // Then
    assertThat(actual)
      .contentHasSize(3)
      .hasTotalPages(1)
      .hasPageNumber(page)
      .hasPageSize(pageSize)
      .hasTotalElements(3)

    assertThat(actual.content[0]).hasNoteContent("Pay attention to Peter's behaviour.")
    assertThat(actual.content[1]).hasNoteContent("Peter is progressing well.")
    assertThat(actual.content[2]).hasNoteContent("Peter has complete his goal.")
    verify(userService, times(3)).getUserDetails("asmith_gen")
    verify(userService, times(3)).getUserDetails("bjones_gen")
  }
}
