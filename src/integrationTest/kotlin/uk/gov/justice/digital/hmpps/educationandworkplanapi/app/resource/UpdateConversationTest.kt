package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.aValidReference
import uk.gov.justice.digital.hmpps.domain.anotherValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.conversation.aValidCreateConversationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.conversation.aValidCreateReviewConversationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.conversation.aValidUpdateConversationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody

class UpdateConversationTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/conversations/{prisonNumber}/{conversationReference}"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.put()
      .uri(URI_TEMPLATE, aValidPrisonNumber(), aValidReference())
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token with view only role`() {
    webTestClient.put()
      .uri(URI_TEMPLATE, aValidPrisonNumber(), aValidReference())
      .withBody(aValidCreateReviewConversationRequest())
      .bearerToken(
        aValidTokenWithAuthority(
          CONVERSATIONS_RO,
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should fail to update Conversation given null fields`() {
    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, aValidPrisonNumber(), aValidReference())
      .bodyValue(
        """
          { }
        """.trimIndent(),
      )
      .bearerToken(
        aValidTokenWithAuthority(
          CONVERSATIONS_RW,
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.BAD_REQUEST.value())
      .hasUserMessageContaining("JSON parse error")
      .hasUserMessageContaining("value failed for JSON property prisonId due to missing (therefore NULL) value for creator parameter prisonId which is a non-nullable type")
  }

  @Test
  fun `should fail to update Conversation given conversation does not exist`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val conversationReference = aValidReference()

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber, conversationReference)
      .withBody(aValidUpdateConversationRequest())
      .bearerToken(
        aValidTokenWithAuthority(
          CONVERSATIONS_RW,
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNotFound
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.NOT_FOUND.value())
      .hasUserMessage("Conversation with reference [$conversationReference] for prisoner [$prisonNumber] not found")
  }

  @Test
  fun `should fail to update Conversation if given conversation exists but does not belong to prisoner`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val anotherPrisonNumber = anotherValidPrisonNumber()

    // When
    createConversation(anotherPrisonNumber, aValidCreateConversationRequest())
    val createdConversation = conversationRepository.findAllByPrisonNumber(anotherPrisonNumber).first()

    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber, createdConversation.reference)
      .withBody(aValidUpdateConversationRequest())
      .bearerToken(
        aValidTokenWithAuthority(
          CONVERSATIONS_RW,
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNotFound
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.NOT_FOUND.value())
      .hasUserMessage("Conversation with reference [${createdConversation.reference}] for prisoner [$prisonNumber] not found")
  }

  @Test
  fun `should update a Conversation for a prisoner`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createReviewConversationRequest = aValidCreateReviewConversationRequest(
      prisonId = "BXI",
      note = "Pay attention to Peter's behaviour.",
    )
    val updateConversationRequest = aValidUpdateConversationRequest(
      prisonId = "MDI",
      note = "Peter's behaviour is improving.",
    )

    val creatorUsername = "auser_gen"
    val creatorName = "Albert User"

    val editorUsername = "bruser_gen"
    val editorName = "Bernie User"

    // When
    createConversation(
      prisonNumber,
      createReviewConversationRequest,
      creatorUsername,
      creatorName,
    )

    val createdConversation = conversationRepository.findAllByPrisonNumber(prisonNumber).first()

    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber, createdConversation.reference)
      .withBody(updateConversationRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          CONVERSATIONS_RW,
          username = editorUsername,
          displayName = editorName,
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent()

    // Then
    val conversation = conversationRepository.findAllByPrisonNumber(prisonNumber).first()
    assertThat(conversation)
      .isForPrisonNumber(prisonNumber)
      .content {
        it.hasContent("Peter's behaviour is improving.")
          .wasCreatedAtPrison("BXI")
          .wasCreatedBy(creatorUsername)
          .hasCreatedByDisplayName(creatorName)
          .wasUpdatedAtPrison("MDI")
          .wasUpdatedBy(editorUsername)
          .hasUpdatedByDisplayName(editorName)
      }
      .wasUpdatedAfter(conversation.createdAt!!)
      .wasCreatedBy(creatorUsername)
      .wasUpdatedBy(editorUsername)
  }
}
