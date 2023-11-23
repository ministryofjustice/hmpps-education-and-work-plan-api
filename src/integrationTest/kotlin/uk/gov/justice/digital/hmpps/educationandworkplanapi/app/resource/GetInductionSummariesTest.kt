package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithNoAuthorities
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithViewAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.anotherValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CiagInductionSummaryListResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork.NO
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork.YES
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCiagInductionSummaryResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateCiagInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidGetCiagInductionSummariesRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody

class GetInductionSummariesTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/ciag/induction/list"
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
    val request = aValidGetCiagInductionSummariesRequest()

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
      .hasDeveloperMessage("Access denied on uri=/ciag/induction/list")
  }

  @Test
  fun `should get empty list of induction summaries given prisoner has no CIAG Induction`() {
    // Given
    val request = aValidGetCiagInductionSummariesRequest(offenderIds = listOf(aValidPrisonNumber()))

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE)
      .withBody(request)
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(CiagInductionSummaryListResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual).hasEmptySummaries()
  }

  @Test
  fun `should get multiple induction summaries`() {
    // Given
    val prisonNumber1 = aValidPrisonNumber()
    val prisonNumber2 = anotherValidPrisonNumber()
    createInduction(prisonNumber1, aValidCreateCiagInductionRequest(hopingToGetWork = NO))
    createInduction(prisonNumber2, aValidCreateCiagInductionRequest(hopingToGetWork = YES))

    val expectedResponse = CiagInductionSummaryListResponse(
      ciagProfileList = listOf(
        aValidCiagInductionSummaryResponse(
          offenderId = prisonNumber1,
          desireToWork = false,
          hopingToGetWork = NO,
          createdBy = "auser_gen", // expected createdBy and modifiedBy will be the user that created the inductions via the `createInduction` method call above
          modifiedBy = "auser_gen",
        ),
        aValidCiagInductionSummaryResponse(
          offenderId = prisonNumber2,
          desireToWork = true,
          hopingToGetWork = YES,
          createdBy = "auser_gen",
          modifiedBy = "auser_gen",
        ),
      ),
    )

    val request = aValidGetCiagInductionSummariesRequest(offenderIds = listOf(prisonNumber1, prisonNumber2))

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE)
      .withBody(request)
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(CiagInductionSummaryListResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual).hasSummaryCount(2)
    assertThat(actual).usingRecursiveComparison()
      .ignoringCollectionOrder()
      .ignoringFieldsMatchingRegexes(".*createdDateTime", ".*modifiedDateTime")
      .isEqualTo(expectedResponse)
  }

  @Test
  fun `should get empty list of induction summaries given request containing no prison numbers`() {
    // Given
    val request = aValidGetCiagInductionSummariesRequest(offenderIds = emptyList())

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE)
      .withBody(request)
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(CiagInductionSummaryListResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual).hasEmptySummaries()
  }
}
