package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.FluxExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithNoAuthorities
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aConvictedOffence
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
  private lateinit var unkownPrisonNumber: String
  private val pesContractStartDate = aPrisonEducationServiceProperties().contractStartDate
  private lateinit var knownPrisoner: Prisoner

  @BeforeEach
  internal fun setUp() {
    prisonNumber = randomValidPrisonNumber()
    knownPrisoner = aValidPrisoner(
      prisonerNumber = prisonNumber,
      receptionDate = pesContractStartDate.plusDays(1),
      sentenceStartDate = pesContractStartDate,
    )

    unkownPrisonNumber = randomValidPrisonNumber()
  }

  @Test
  fun `should return unauthorised given no bearer token`() {
    // When
    getAssessmentsRequired(prisonNumber, bearerToken = null)
      .expectStatus().isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token without required role`() {
    // When
    val bearerToken = aValidTokenWithNoAuthorities(privateKey = keyPair.private)
    val response = getAssessmentsRequired(prisonNumber, bearerToken)
      .expectStatus().isForbidden
      .returnError()

    // Then
    val actual = response.body()
    assertThat(actual)
      .hasStatus(HttpStatus.FORBIDDEN.value())
      .hasUserMessage("Access Denied")
      .hasDeveloperMessage("Access denied on uri=/assessments/$prisonNumber/required")
  }

  @Test
  fun `should return not found given prisoner does not exist`() {
    // Given
    wiremockService.stubGetPrisonerNotFound(unkownPrisonNumber)

    // When
    val response = getAssessmentsRequired(unkownPrisonNumber)
      .expectStatus().isNotFound
      .returnError()

    // Then
    val actual = response.body()
    assertThat(actual)
      .hasStatus(HttpStatus.NOT_FOUND.value())
      .hasUserMessage("Prisoner [$unkownPrisonNumber] not returned by Prisoner Search API")
  }

  @Test
  fun `should return BSA eligibility for prisoner, even when reception date is missing`() {
    // Given
    val prisoner =
      aValidPrisoner(prisonerNumber = prisonNumber, allConvictedOffences = listOf(aConvictedOffence())).copy(
        receptionDate = null,
      )
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(prisonNumber, prisoner)

    // When
    val response = getAssessmentsRequiredIsOk(prisonNumber)

    // Then
    val actual = response.body()
    assertThat(actual.basicSkillsAssessmentRequired).isNotNull
  }

  @Test
  fun `should return bad request for prisoner without both sentence start date and reception date`() {
    // Given
    val prisoner = aValidPrisoner(prisonerNumber = prisonNumber).copy(sentenceStartDate = null, receptionDate = null)
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(prisonNumber, prisoner)

    // When
    val response = getAssessmentsRequiredIsBadRequest(prisoner.prisonerNumber)

    // Then
    val actual = response.body()
    assertThat(actual)
      .hasStatus(HttpStatus.BAD_REQUEST.value())
      .hasUserMessage("Sentence start date and Reception date of Prisoner [$prisonNumber] are both missing.")
  }

  @Test
  fun `should return BSA eligibility for prisoner`() {
    // Given
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(prisonNumber, knownPrisoner)

    // When
    val response = getAssessmentsRequiredIsOk(prisonNumber)

    // Then
    val actual = response.body()
    assertThat(actual.basicSkillsAssessmentRequired).isTrue()
  }

  @Nested
  @DisplayName("Given upstream error (from Prisoner Search)")
  inner class GivenUpstreamError {
    @Test
    fun `should return BSA eligibility for prisoner, given earlier connection reset by peer (RST)`() {
      // Given
      val numberOfRequests = 3
      // Retry after RST error, succeed at last
      wiremockService.stubGetPrisonerWithConnectionResetError(prisonNumber, knownPrisoner, numberOfRequests)

      // When
      val response = getAssessmentsRequiredIsOk(prisonNumber)

      // Then
      val actual = response.body()
      assertThat(actual.basicSkillsAssessmentRequired).isNotNull
      wiremockService.verifyGetPrisoner(numberOfRequests)
    }

    @Test
    fun `should return BSA eligibility for prisoner, given earlier connection timed out`() {
      // Given
      val numberOfRequests = 3
      // Retry after response timed out, succeed at last
      wiremockService.stubGetPrisonerWithConnectionTimedOutError(prisonNumber, knownPrisoner, numberOfRequests)

      // When
      val response = getAssessmentsRequiredIsOk(prisonNumber)

      // Then
      val actual = response.body()
      assertThat(actual.basicSkillsAssessmentRequired).isNotNull
      wiremockService.verifyGetPrisoner(numberOfRequests)
    }
  }

  private val aValidBearerToken
    get() = aValidTokenWithAuthority(
      ASSESSMENTS_RO,
      privateKey = keyPair.private,
    )

  private fun getAssessmentsRequired(
    prisonNumber: String,
    bearerToken: String? = aValidBearerToken,
  ) = webTestClient.get()
    .uri(URI_TEMPLATE, prisonNumber)
    .let { bearerToken?.let { bearerToken -> it.bearerToken(bearerToken) } ?: it }
    .exchange()

  private fun getAssessmentsRequiredIsOk(prisonNumber: String) = getAssessmentsRequired(prisonNumber, aValidBearerToken)
    .expectStatus().isOk
    .returnEducationAssessmentRequired()

  private fun getAssessmentsRequiredIsBadRequest(prisonNumber: String) = getAssessmentsRequired(prisonNumber)
    .expectStatus().isBadRequest
    .returnError()

  private fun WebTestClient.ResponseSpec.returnEducationAssessmentRequired() = this.returnResult(EducationAssessmentRequired::class.java)
  private fun WebTestClient.ResponseSpec.returnError() = this.returnResult(ErrorResponse::class.java)
  private fun <T> FluxExchangeResult<T>.body(): T = this.responseBody.blockFirst()!!
}
