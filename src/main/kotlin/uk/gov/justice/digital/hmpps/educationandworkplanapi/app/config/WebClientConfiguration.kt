package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration
import kotlin.apply as kotlinApply

private const val DEFAULT_TIMEOUT_SECONDS: Long = 30

@Configuration
class WebClientConfiguration(
  private val apiProperties: ApiProperties,
  private val apisProperties: ApisProperties,
) {
  @Bean(name = ["prisonApiWebClient"])
  fun prisonApiWebClient(
    authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient = builder.authorisedWebClient(
    authorizedClientManager,
    registrationId = "prison-api",
    url = apisProperties.prisonApi.url,
    timeout = apiProperties.timeout,
  )

  @Bean(name = ["manageUsersApiWebClient"])
  fun manageUsersApiWebClient(
    authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient = builder.authorisedWebClient(
    authorizedClientManager,
    registrationId = "manage-users-api",
    url = apisProperties.manageUsersApi.url,
    timeout = apiProperties.timeout,
  )

  @Bean(name = ["prisonerSearchApiWebClient"])
  fun prisonerSearchApiWebClient(
    authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient = builder.authorisedWebClient(
    authorizedClientManager,
    registrationId = "prisoner-search-api",
    url = apisProperties.prisonerSearchApi.url,
    timeout = apiProperties.timeout,
  )

  @Bean
  fun authorizedClientManager(
    clientRegistrationRepository: ClientRegistrationRepository,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
  ): OAuth2AuthorizedClientManager {
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()
    return AuthorizedClientServiceOAuth2AuthorizedClientManager(
      clientRegistrationRepository,
      oAuth2AuthorizedClientService,
    ).kotlinApply { setAuthorizedClientProvider(authorizedClientProvider) }
  }

  private fun WebClient.Builder.authorisedWebClient(
    authorizedClientManager: OAuth2AuthorizedClientManager,
    registrationId: String,
    url: String,
    timeout: Duration = Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS),
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager).kotlinApply {
      setDefaultClientRegistrationId(registrationId)
    }

    return baseUrl(url)
      .clientConnector(ReactorClientHttpConnector(HttpClient.create().responseTimeout(timeout)))
      .filter(oauth2Client)
      .build()
  }
}
