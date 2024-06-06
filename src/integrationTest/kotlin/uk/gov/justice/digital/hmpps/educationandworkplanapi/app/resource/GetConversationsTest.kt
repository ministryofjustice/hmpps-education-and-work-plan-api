package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithEditAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ConversationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.conversation.aValidCreateReviewConversationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.conversation.assertThat

class GetConversationsTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/conversations/{prisonNumber}"
    private const val URI_TEMPLATE_WITH_QUERY_STRING = "/conversations/{prisonNumber}?page={pageNumber}&pageSize={pageSize}"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.get()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should get a page of conversations for a prisoner`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val conversation1 = aValidCreateReviewConversationRequest(
      note = "Pay attention to Peter's behaviour.",
    )
    val conversation2 = aValidCreateReviewConversationRequest(
      note = "Peter is progressing well.",
    )
    val conversation3 = aValidCreateReviewConversationRequest(
      note = "Peter has complete his goal.",
    )

    // When
    listOf(conversation1, conversation2, conversation3).forEach {
      createConversation(prisonNumber, it)
    }

    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(
        aValidTokenWithEditAuthority(
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(ConversationsResponse::class.java)
      .responseBody.blockFirst()!!

    // Then
    assertThat(response).hasPageNumber(0)
    assertThat(response).hasPageSize(20)
    assertThat(response).hasTotalPages(1)
    assertThat(response).hasTotalElements(3)

    assertThat(response.content[0]).hasNoteContent("Pay attention to Peter's behaviour.")
    assertThat(response.content[1]).hasNoteContent("Peter is progressing well.")
    assertThat(response.content[2]).hasNoteContent("Peter has complete his goal.")
  }

  @Test
  fun `should get the second page of conversations for a prisoner`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val numberOfConversations = 24
    val pageNumber = 1
    val pageSize = 20

    // When
    repeat(numberOfConversations) {
      createConversation(prisonNumber)
    }

    val response = webTestClient.get()
      .uri(URI_TEMPLATE_WITH_QUERY_STRING, prisonNumber, pageNumber, pageSize)
      .bearerToken(
        aValidTokenWithEditAuthority(
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(ConversationsResponse::class.java)
      .responseBody.blockFirst()!!

    // Then
    assertThat(response).hasPageNumber(pageNumber)
    assertThat(response).hasPageSize(pageSize)
    assertThat(response).hasTotalPages(2)
    assertThat(response).hasTotalElements(numberOfConversations)
    assertThat(response).contentHasSize(4)
  }

  @Test
  fun `should get a page of conversations for a prisoner that doesn't contain content`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val numberOfConversations = 5
    val pageNumber = 5
    val pageSize = 20

    // When
    repeat(numberOfConversations) {
      createConversation(prisonNumber)
    }

    val response = webTestClient.get()
      .uri(URI_TEMPLATE_WITH_QUERY_STRING, prisonNumber, pageNumber, pageSize)
      .bearerToken(
        aValidTokenWithEditAuthority(
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(ConversationsResponse::class.java)
      .responseBody.blockFirst()!!

    // Then
    assertThat(response).hasPageNumber(pageNumber)
    assertThat(response).hasPageSize(pageSize)
    assertThat(response).hasTotalPages(1)
    assertThat(response).hasTotalElements(numberOfConversations)
    assertThat(response).contentHasSize(0)
  }
}
