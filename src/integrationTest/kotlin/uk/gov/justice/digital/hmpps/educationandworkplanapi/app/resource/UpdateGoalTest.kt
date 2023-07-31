package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithViewAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidUpdateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody

class UpdateGoalTest : IntegrationTestBase() {

  companion object {
    private const val URI_TEMPLATE = "/action-plans/{prisonNumber}/goals/{goalReference}"
  }

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.put()
      .uri(URI_TEMPLATE, aValidPrisonNumber(), aValidReference())
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token with view only role`() {
    webTestClient.put()
      .uri(URI_TEMPLATE, aValidPrisonNumber(), aValidReference())
      .withBody(aValidUpdateGoalRequest())
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isForbidden
  }
}
