package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithEditAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithViewAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInductionEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateCiagInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody

class CreateInductionTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/ciag/induction/{prisonNumber}"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.post()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token with view only role`() {
    webTestClient.post()
      .uri(URI_TEMPLATE, aValidPrisonNumber())
      .withBody(aValidCreateCiagInductionRequest())
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should fail to create induction given no induction data provided`() {
    val prisonNumber = aValidPrisonNumber()

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .bodyValue(
        """
          { }
        """.trimIndent(),
      )
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(BAD_REQUEST.value())
      .hasUserMessageContaining("JSON parse error")
      .hasUserMessageContaining("value failed for JSON property prisonId due to missing (therefore NULL) value for creator parameter prisonId")
  }

  @Test
  @Transactional
  fun `should fail to create induction given induction already exists`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val induction = aValidInductionEntity()
    inductionRepository.save(induction)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
    val createRequest = aValidCreateCiagInductionRequest()

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(FORBIDDEN.value())
      .hasUserMessage("An Induction already exists for prisoner $prisonNumber")
  }

  @Test
  @Transactional
  fun `should create a new induction given prisoner does not have an induction`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createRequest = aValidCreateCiagInductionRequest()
    val dpsUsername = "auser_gen"
    val displayName = "Albert User"
    val prisonId = createRequest.prisonId

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(
        aValidTokenWithEditAuthority(
          username = dpsUsername,
          displayName = displayName,
          privateKey = keyPair.private,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val induction = inductionRepository.findByPrisonNumber(prisonNumber)
    assertThat(induction)
      .isForPrisonNumber(prisonNumber)
      .hasAReference()
      .hasJpaManagedFieldsPopulated()
      .wasCreatedBy(dpsUsername)
      .wasUpdatedBy(dpsUsername)
      .wasCreatedAtPrison(prisonId)
      .wasUpdatedAtPrison(prisonId)

    await.untilAsserted {
      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)
      verify(telemetryClient, times(1)).trackEvent(
        eq("induction-created"),
        capture(eventPropertiesCaptor),
        eq(null),
      )
      val createInductionEventProperties = eventPropertiesCaptor.firstValue
      assertThat(createInductionEventProperties)
        .containsEntry("prisonNumber", prisonNumber)
        .containsEntry("prisonId", prisonId)
        .containsEntry("userId", dpsUsername)
        .containsKey("correlationId")
        .containsKey("timestamp")
    }
  }
}
