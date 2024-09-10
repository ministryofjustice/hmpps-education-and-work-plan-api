package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.ConversationType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.conversation.aValidCreateReviewConversationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody

class CreateConversationTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/conversations/{prisonNumber}"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.post()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token with view only role`() {
    webTestClient.post()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .withBody(aValidCreateReviewConversationRequest())
      .bearerToken(aValidTokenWithAuthority(CONVERSATIONS_RO, privateKey = keyPair.private))
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should fail to create Conversation given null fields`() {
    val prisonNumber = aValidPrisonNumber()

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .bodyValue(
        """
          { }
        """.trimIndent(),
      )
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private))
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
  fun `should create a new Conversation for prisoner`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val prisonId = "BXI"
    val createReviewConversationRequest = aValidCreateReviewConversationRequest(prisonId = prisonId)
    val dpsUsername = "auser_gen"
    val userDisplayName = "Albert User"

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createReviewConversationRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          CONVERSATIONS_RW,
          username = dpsUsername,
          displayName = userDisplayName,
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val conversation = conversationRepository.findAllByPrisonNumber(prisonNumber).first()
    assertThat(conversation)
      .isForPrisonNumber(prisonNumber)
      .isOfType(ConversationType.REVIEW)
      .wasCreatedBy(dpsUsername)
      .content {
        it.wasCreatedBy(dpsUsername)
          .hasCreatedByDisplayName(userDisplayName)
          .wasCreatedAtPrison(prisonId)
          .wasUpdatedBy(dpsUsername)
          .hasUpdatedByDisplayName(userDisplayName)
          .wasUpdatedAtPrison(prisonId)
      }
  }
}
