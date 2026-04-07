package uk.gov.justice.digital.hmpps.educationandworkplanapi.app

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken

class NotFoundTest : IntegrationTestBase() {

  @Test
  fun `Resources that aren't found should return 404 - test of the exception handler`() {
    webTestClient.get().uri("/some-url-not-found")
      .bearerToken(aValidTokenWithNoAuthorities())
      .exchange()
      .expectStatus().isNotFound
  }
}
