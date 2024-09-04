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
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.anotherValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HasWorkedBefore
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreatePreviousWorkExperiencesRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody

class CreateInductionTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/inductions/{prisonNumber}"
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
      .withBody(aValidCreateInductionRequest())
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RO, privateKey = keyPair.private))
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
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RW, privateKey = keyPair.private))
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
  fun `should fail to create induction given induction already exists`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork())

    val createRequest = aValidCreateInductionRequestForPrisonerNotLookingToWork()

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RW, privateKey = keyPair.private))
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
  fun `should fail to create induction given malformed prison id`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    val invalidPrisonId = "does not meet pattern of 3 upper case letters"
    val createRequest = aValidCreateInductionRequestForPrisonerNotLookingToWork(prisonId = invalidPrisonId)

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(BAD_REQUEST.value())
      .hasUserMessageContaining("Validation failed for object='createInductionRequest'")
      .hasDeveloperMessage("[Error on field 'prisonId': rejected value [does not meet pattern of 3 upper case letters], must match \"^[A-Z]{3}\$\"]")
  }

  @Test
  fun `should create an induction for a prisoner looking for work`() {
    // Given
    val prisonNumber = anotherValidPrisonNumber()
    val createRequest = aValidCreateInductionRequestForPrisonerLookingToWork()
    val dpsUsername = "auser_gen"
    val displayName = "Albert User"

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          INDUCTIONS_RW,
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
    val induction = getInduction(prisonNumber)
    assertThat(induction)
      .wasCreatedBy(dpsUsername)
      .wasUpdatedBy(dpsUsername)
      .wasCreatedAtPrison(createRequest.prisonId)
      .wasUpdatedAtPrison(createRequest.prisonId)

    await.untilAsserted {
      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)
      verify(telemetryClient, times(1)).trackEvent(
        eq("INDUCTION_CREATED"),
        capture(eventPropertiesCaptor),
        eq(null),
      )
      val createInductionEventProperties = eventPropertiesCaptor.firstValue
      assertThat(createInductionEventProperties)
        .containsEntry("prisonId", createRequest.prisonId)
        .containsEntry("userId", dpsUsername)
        .containsKey("reference")
    }
  }

  @Test
  fun `should create an induction for a prisoner not looking for work`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createRequest = aValidCreateInductionRequestForPrisonerNotLookingToWork()
    val dpsUsername = "auser_gen"
    val displayName = "Albert User"

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          INDUCTIONS_RW,
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
    val induction = getInduction(prisonNumber)
    assertThat(induction)
      .wasCreatedBy(dpsUsername)
      .wasUpdatedBy(dpsUsername)
      .wasCreatedAtPrison(createRequest.prisonId)
      .wasUpdatedAtPrison(createRequest.prisonId)

    await.untilAsserted {
      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)
      verify(telemetryClient, times(1)).trackEvent(
        eq("INDUCTION_CREATED"),
        capture(eventPropertiesCaptor),
        eq(null),
      )
      val createInductionEventProperties = eventPropertiesCaptor.firstValue
      assertThat(createInductionEventProperties)
        .containsEntry("prisonId", createRequest.prisonId)
        .containsEntry("userId", dpsUsername)
        .containsKey("reference")
    }
  }

  @Test
  fun `should create an induction for a prisoner whose previous work experience is declared as not relevant`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val createRequest = aValidCreateInductionRequestForPrisonerNotLookingToWork(
      previousWorkExperiences = aValidCreatePreviousWorkExperiencesRequest(
        hasWorkedBefore = HasWorkedBefore.NOT_RELEVANT,
        hasWorkedBeforeNotRelevantReason = "Prisoner is not looking for work so feels previous work experience is not relevant",
        experiences = emptyList(),
      ),
    )

    // When
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(createRequest)
      .bearerToken(
        aValidTokenWithAuthority(INDUCTIONS_RW, privateKey = keyPair.private),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()

    // Then
    val induction = getInduction(prisonNumber)
    assertThat(induction)
      .previousWorkExperiences {
        it.hasWorkedBefore(HasWorkedBefore.NOT_RELEVANT)
          .hasWorkedBeforeNotRelevantReason("Prisoner is not looking for work so feels previous work experience is not relevant")
      }
  }
}
