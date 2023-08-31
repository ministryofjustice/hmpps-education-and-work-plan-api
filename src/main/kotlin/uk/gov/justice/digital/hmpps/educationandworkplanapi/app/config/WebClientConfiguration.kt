package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfiguration {

  @Bean
  fun hmppsAuthWebClient(@Value("\${api.hmpps-auth.url}") eroManagementApiUrl: String): WebClient =
    WebClient.builder()
      .baseUrl(eroManagementApiUrl)
      .build()
}

fun WebClient.RequestHeadersSpec<*>.bearerToken(bearerToken: String): WebClient.RequestBodySpec =
  header("authorization", "Bearer $bearerToken") as WebClient.RequestBodySpec
