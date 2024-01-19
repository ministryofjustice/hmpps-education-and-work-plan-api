package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class TestWebClientConfiguration {

  /**
   * Override the main [WebClient] configuration, as we do not need OAuth2 authorisation for Wiremock.
   */
  @Bean
  @Primary
  fun testPrisonApiWebClient(
    @Value("\${apis.prison-api.url}") prisonApiUri: String,
    builder: WebClient.Builder,
  ): WebClient =
    builder.baseUrl(prisonApiUri).build()
}
