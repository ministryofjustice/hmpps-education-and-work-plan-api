package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithNoAuthorities
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanSummaryListResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidActionPlanSummaryResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidGetActionPlanSummariesRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody

class GetActionPlanSummariesTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/action-plans"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.post()
      .uri(URI_TEMPLATE)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token without required role`() {
    // Given
    val request = aValidGetActionPlanSummariesRequest()

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE)
      .withBody(request)
      .bearerToken(aValidTokenWithNoAuthorities(privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isForbidden
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(FORBIDDEN.value())
      .hasUserMessage("Access Denied")
      .hasDeveloperMessage("Access denied on uri=/action-plans")
  }

  @Test
  fun `should get empty list of action plan summaries given prisoner has no action plan`() {
    // Given
    val request = aValidGetActionPlanSummariesRequest(prisonNumbers = listOf(randomValidPrisonNumber()))

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE)
      .withBody(request)
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RO, privateKey = keyPair.private))
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(ActionPlanSummaryListResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual).hasEmptySummaries()
  }

  @Test
  fun `should get multiple action plan summaries`() {
    // Given
    val prisonNumber1 = randomValidPrisonNumber()
    val prisonNumber2 = randomValidPrisonNumber()
    createActionPlan(prisonNumber1, aValidCreateActionPlanRequest())
    createActionPlan(prisonNumber2, aValidCreateActionPlanRequest())
    val request = aValidGetActionPlanSummariesRequest(listOf(prisonNumber1, prisonNumber2))
    val expectedResponse = ActionPlanSummaryListResponse(
      actionPlanSummaries = listOf(
        aValidActionPlanSummaryResponse(prisonNumber = prisonNumber1),
        aValidActionPlanSummaryResponse(prisonNumber = prisonNumber2),
      ),
    )

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE)
      .withBody(request)
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RO, privateKey = keyPair.private))
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(ActionPlanSummaryListResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual).hasSummaryCount(2)
    assertThat(actual).usingRecursiveComparison()
      .ignoringCollectionOrder()
      .ignoringFields("actionPlanSummaries.reference")
      .isEqualTo(expectedResponse)
  }

  @Test
  fun `should get empty list of action plan summaries given request containing no prison numbers`() {
    // Given
    val request = aValidGetActionPlanSummariesRequest(prisonNumbers = emptyList())

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE)
      .withBody(request)
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RO, privateKey = keyPair.private))
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(ActionPlanSummaryListResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual).hasEmptySummaries()
  }
}
