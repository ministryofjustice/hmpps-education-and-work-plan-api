package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.aValidReference
import uk.gov.justice.digital.hmpps.domain.anotherValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithEditAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ConversationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.conversation.aValidCreateConversationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.conversation.aValidCreateReviewConversationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.conversation.assertThat
import java.time.OffsetDateTime

class GetConversationTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/conversations/{prisonNumber}/{reference}"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.get()
      .uri(URI_TEMPLATE, aValidPrisonNumber(), aValidReference())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should fail to get conversation given conversation does not exist`() {
    // Given
    var prisonNumber = aValidPrisonNumber()
    val conversationReference = aValidReference()

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber, conversationReference)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
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
  fun `should fail to get conversation if given conversation exists but does not belong to prisoner`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val anotherPrisonNumber = anotherValidPrisonNumber()

    // When
    createConversation(anotherPrisonNumber, aValidCreateConversationRequest())
    val createdConversation = conversationRepository.findAllByPrisonNumber(anotherPrisonNumber).first()

    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber, createdConversation.reference)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
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
  fun `should get conversation`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val note = "Adam is progressing well"
    val createReviewConversationRequest = aValidCreateReviewConversationRequest(
      note = note,
    )
    val createUsername = "auser_gen"
    val createDisplayName = "Albert User"
    val initialDateTime = OffsetDateTime.now()

    // When
    createConversation(
      prisonNumber,
      createReviewConversationRequest,
    )

    val createdConversation = conversationRepository.findAllByPrisonNumber(prisonNumber).first()

    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber, createdConversation.reference)
      .bearerToken(
        aValidTokenWithEditAuthority(
          privateKey = keyPair.private,
          username = createUsername,
          displayName = createDisplayName,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(ConversationResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .isForPrisonNumber(prisonNumber)
      .hasNoteContent(note)
      .wasCreatedAtPrison(createReviewConversationRequest.prisonId)
      .wasCreatedAfter(initialDateTime)
      .wasCreatedBy(createUsername)
      .hasCreatedByDisplayName(createDisplayName)
      .wasUpdatedAtPrison(createReviewConversationRequest.prisonId)
      .wasUpdatedAfter(initialDateTime)
      .wasUpdatedBy(createUsername)
      .hasUpdatedByDisplayName(createDisplayName)
  }
}
