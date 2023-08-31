package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase

class GetSwaggerSpecTest : IntegrationTestBase() {

  @Test
  fun `should get swagger spec in json with no bearer token`() {
    webTestClient.get()
      .uri("/v3/api-docs")
      .exchange()
      .expectStatus()
      .isOk
  }

  @Test
  fun `should get swagger ui with no bearer token`() {
    webTestClient.get()
      .uri("/swagger-ui/index.html")
      .exchange()
      .expectStatus()
      .isOk
  }
}
