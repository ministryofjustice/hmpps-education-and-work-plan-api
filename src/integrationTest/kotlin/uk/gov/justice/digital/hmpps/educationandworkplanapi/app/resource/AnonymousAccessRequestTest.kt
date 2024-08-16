package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase

/**
 * Integration tests that make HTTP requests against the API for resources that are designed to be accessed anonymously
 * without a bearer token.
 */
class AnonymousAccessRequestTest : IntegrationTestBase() {

  @ParameterizedTest
  @ValueSource(
    strings = [
      "/v3/api-docs",
      "/swagger-ui.html",
      "/swagger-ui/index.html",
      "/swagger-ui/swagger-ui-bundle.js",
      "/openapi/EducationAndWorkPlanAPI.yml",
    ],
  )
  fun `should make GET request to swagger spec related endpoints given request with no bearer token`(uri: String) {
    webTestClient.get()
      .uri(uri)
      .exchange()
      .expectStatus()
      .value { statusCode ->
        assertThat(statusCode).satisfies({ it == 200 || it == 302 }) // Most swagger URIs return a 200, but /swagger-ui.html returns a 302 redirect to /swagger-ui/index.html
      }
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "/info",
      "/health",
    ],
  )
  fun `should make GET request to spring boot actuator endpoints given request with no bearer token`(uri: String) {
    webTestClient.get()
      .uri(uri)
      .exchange()
      .expectStatus()
      .isOk
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "/queue-admin/retry-all-dlqs",
    ],
  )
  fun `should make PUT request to HMPPS SQS queue housekeeping endpoints given request with no bearer token`(uri: String) {
    webTestClient.put()
      .uri(uri)
      .exchange()
      .expectStatus()
      .isOk
  }
}
