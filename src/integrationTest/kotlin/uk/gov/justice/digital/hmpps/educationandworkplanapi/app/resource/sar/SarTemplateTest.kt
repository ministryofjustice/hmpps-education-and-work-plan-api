package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.sar

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat

class SarTemplateTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/subject-access-request/template"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.get()
      .uri(URI_TEMPLATE)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token without required role`() {
    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE)
      .bearerToken(aValidTokenWithNoAuthorities())
      .exchange()
      .expectStatus()
      .isForbidden
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.FORBIDDEN.value())
      .hasUserMessage("Access Denied")
      .hasDeveloperMessage("Access denied on uri=/subject-access-request/template")
  }

  @Test
  fun `should return template`() {
    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE)
      .bearerToken(aValidTokenWithAuthority("ROLE_SAR_DATA_ACCESS"))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader()
      .contentType(MediaType.TEXT_PLAIN)
      .returnResult(String::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual).isNotEmpty
  }
}
