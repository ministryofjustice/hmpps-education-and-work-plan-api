package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.http.HttpStatus.BAD_REQUEST
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat

/**
 * Integration tests that test and assert the validation of the prisonNumber path parameter in our API endpoints.
 *
 * All API endpoints that accept the prisonNumber as a path variable parameter include this validation. Rather than testing
 * every endpoint, one GET endpoint is tested here with the assumption that all others will validate in the same way.
 */
class PrisonNumberPathVariableParameterValidationTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/action-plans/{prisonNumber}"
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      // Non-malicious but malformed prison numbers
      "A1234B",
      "1234",
      "a malformed prison number",
    ],
  )
  fun `should reject request with BAD_REQUEST given prison number is non-malicious but malformed`(malformedPrisonNumber: String) {
    // Given

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, malformedPrisonNumber)
      .bearerToken(
        aValidTokenWithAuthority(
          ACTIONPLANS_RW,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(BAD_REQUEST.value())
      .hasUserMessageContaining("must match \"^[A-Z]\\d{4}[A-Z]{2}\$\"")
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      // Examples of reflected payload style attacks
      "<script>alert('Attempt to do something something bad')</script>",
      "<html><body>Attempt to render back an entirely different document</body></html>",
    ],
    // It may be because the malformed prison number is HTML in these scenarios, but for some reason the application server
    // interprets these requests as content type text/html, hence the response body.
  )
  fun `should reject request with BAD_REQUEST given prison number is at attempt at a reflected payload attack`(malformedPrisonNumber: String) {
    // Given

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, malformedPrisonNumber)
      .bearerToken(
        aValidTokenWithAuthority(
          ACTIONPLANS_RW,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(String::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual).isEqualTo(
      """<!doctype html><html lang="en"><head><title>HTTP Status 400 – Bad Request</title><style type="text/css">body {font-family:Tahoma,Arial,sans-serif;} h1, h2, h3, b {color:white;background-color:#525D76;} h1 {font-size:22px;} h2 {font-size:16px;} h3 {font-size:14px;} p {font-size:12px;} a {color:black;} .line {height:1px;background-color:#525D76;border:none;}</style></head><body><h1>HTTP Status 400 – Bad Request</h1></body></html>""",
    )
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      // Examples of SQL injection style attacks
      "A1234BC OR 1=1",
      "\" OR \"\"=\"",
      "' OR ''='",
    ],
  )
  fun `should reject request with BAD_REQUEST given prison number is an attempt at a SQL injection attack without semicolons`(malformedPrisonNumber: String) {
    // Given

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, malformedPrisonNumber)
      .bearerToken(
        aValidTokenWithAuthority(
          ACTIONPLANS_RW,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(BAD_REQUEST.value())
      .hasUserMessageContaining("must match \"^[A-Z]\\d{4}[A-Z]{2}\$\"")
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      // Examples of SQL injection style attacks
      "\"; DROP TABLE induction;",
      "'; DROP TABLE induction;",
      "A1234BC; DROP TABLE induction;",
    ],
    // It may be because the malformed prison number contains a semicolon in these scenarios, but for some reason the application server
    // interprets these requests in such a way that it doesn't match the path at all, hence the 401 with a null body response.
  )
  fun `should reject request with UNAUTHORISED given prison number is an attempt at a SQL injection attack`(malformedPrisonNumber: String) {
    // Given

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, malformedPrisonNumber)
      .bearerToken(
        aValidTokenWithAuthority(
          ACTIONPLANS_RW,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isUnauthorized
      .returnResult(Void::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual).isNull()
  }
}
