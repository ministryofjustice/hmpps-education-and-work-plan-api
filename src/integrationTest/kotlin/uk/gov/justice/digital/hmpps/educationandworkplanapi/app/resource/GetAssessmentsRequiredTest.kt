package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithNoAuthorities
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.aPrisonEducationServiceProperties
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationAssessmentRequired
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat

private const val URI_TEMPLATE = "/assessments/{prisonNumber}/required"

class GetAssessmentsRequiredTest : IntegrationTestBase() {
  private lateinit var prisonNumber: String
  private val pesContractStartDate = aPrisonEducationServiceProperties().contractStartDate

  @BeforeEach
  internal fun setUp() {
    prisonNumber = randomValidPrisonNumber()
  }

  @Test
  fun `should return unauthorised given no bearer token`() {
    // When
    webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token without required role`() {
    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithNoAuthorities(privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isForbidden
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.FORBIDDEN.value())
      .hasUserMessage("Access Denied")
      .hasDeveloperMessage("Access denied on uri=/assessments/$prisonNumber/required")
  }

  @Test
  fun `should return not found given prisoner does not exist`() {
    // Given
    wiremockService.stubGetPrisonerNotFound(prisonNumber)

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidBearerToken)
      .exchange()
      .expectStatus()
      .isNotFound
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.NOT_FOUND.value())
      .hasUserMessage("Prisoner [$prisonNumber] not returned by Prisoner Search API")
  }

  @Test
  fun `should return BSA eligibility for prisoner, even when reception date is missing`() {
    // Given
    val prisoner = aValidPrisoner(prisonerNumber = prisonNumber).copy(receptionDate = null)
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(prisonNumber, prisoner)

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidBearerToken)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(EducationAssessmentRequired::class.java)

    // Then
    val actual = response.responseBody.blockFirst()!!
    assertThat(actual.basicSkillsAssessmentRequired).isNotNull
  }

  @Test
  fun `should return bad request for prisoner without both sentence start date and reception date`() {
    // Given
    val prisoner = aValidPrisoner(prisonerNumber = prisonNumber).copy(sentenceStartDate = null, receptionDate = null)
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(prisonNumber, prisoner)

    // When
    val response = assertGetIsBadRequest()

    // Then
    val actual = response.responseBody.blockFirst()!!
    assertThat(actual)
      .hasStatus(HttpStatus.BAD_REQUEST.value())
      .hasUserMessage("Sentence start date and Reception date of Prisoner [$prisonNumber] are both missing.")
  }

  @Test
  fun `should return BSA eligibility for prisoner`() {
    // Given
    val prisoner = aValidPrisoner(
      prisonerNumber = prisonNumber,
      receptionDate = pesContractStartDate.plusDays(1),
      sentenceStartDate = pesContractStartDate,
    )
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(prisonNumber, prisoner)

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidBearerToken)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(EducationAssessmentRequired::class.java)

    // Then
    val actual = response.responseBody.blockFirst()!!
    assertThat(actual.basicSkillsAssessmentRequired).isTrue()
  }

  private fun assertGetIsBadRequest() = webTestClient.get()
    .uri(URI_TEMPLATE, prisonNumber)
    .bearerToken(aValidBearerToken)
    .exchange()
    .expectStatus()
    .isBadRequest
    .returnResult(ErrorResponse::class.java)

  private val aValidBearerToken get() = aValidTokenWithAuthority(
    ASSESSMENTS_RO,
    privateKey = keyPair.private,
  )
}
