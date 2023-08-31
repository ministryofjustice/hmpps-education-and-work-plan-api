package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.bearerToken

/**
 * Client class for interacting with `hmpps-auth`s REST API
 */
@Component
class HmppsAuthClient(private val hmppsAuthWebClient: WebClient) {

  /**
   * Calls the `/user/me` endpoint with the given user token, and returns the user's active caseload ID from the response.
   */
  fun getUserActiveCaseLoadId(userToken: String): String? {
    val userProfile = hmppsAuthWebClient
      .get()
      .uri("/api/user/me")
      .bearerToken(userToken)
      .retrieve()
      .bodyToMono(Map::class.java)
      .block()

    return userProfile?.get("activeCaseLoadId")?.toString()
  }
}
