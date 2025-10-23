package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client

import com.github.tomakehurst.wiremock.client.WireMock.exactly
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.matching.UrlPattern
import io.netty.channel.ChannelOption
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.ClassOrderer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestClassOrder
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.netty.http.client.HttpClient
import reactor.util.retry.Retry
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ApiMockServerExtension
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ApiMockServerExtension.Companion.apiMockServer
import java.time.Duration

@ExtendWith(ApiMockServerExtension::class)
class WebClientExtensionShould {
  private val webClient: WebClient = TestWebClient(apiMockServer.baseUrl(), connectTimeoutMillis = 100, responseTimeoutMillis = 150).client
  private val unreachableWebClient: WebClient = TestWebClient(baseUrl = "http://10.255.255.1:81", connectTimeoutMillis = 1, responseTimeoutMillis = 1).client

  private val webClientExtension = WebClientExtension(
    maxRetryAttempts = 2,
    minBackOffDuration = Duration.ofMillis(5),
    statusCodeRetryExhausted = 599,
  )

  @Nested
  @DisplayName("Retry upstream's idempotent request when necessary")
  @TestClassOrder(ClassOrderer.OrderAnnotation::class)
  inner class RetryUpstreamIdempotentRequest {
    private val upstream = "TestApi"
    private val body = """{"success": true}"""

    @Nested
    @DisplayName("Given successful response from upstream")
    @Order(1)
    inner class GivenResponseFromUpstream {
      private val getPath = "/path"
      private val getPathById = "/path/{id}"
      private val getPathByIdPattern = urlPathMatching("/path/([A-Za-z0-9])+")

      @Test
      fun `receive response from upstream GET, without retry`() {
        apiMockServer.stubForGet(getPath, body)
        val result = getRequestWithRetry(uri = getPath)
        assertTrue(result.success)
        verifyApiGetPath(url = getPath)
      }

      @Test
      fun `receive response from upstream GET with argument, without retry`() {
        apiMockServer.stubForGet(getPathByIdPattern, body)
        val result = getRequestWithRetry(getPathById, "ID123")
        assertTrue(result.success)
        verifyApiGetPathById(urlPattern = getPathByIdPattern)
      }
    }

    @Nested
    @DisplayName("Given an error response from upstream")
    @Order(2)
    inner class GivenErrorResponseReceived {
      private val getPath2 = "/path2"
      private val getPath2ById = "/path2/{id}"
      private val getPath2ByIdPattern = urlPathMatching("/path2/([A-Za-z0-9])+")

      @ParameterizedTest
      @ValueSource(ints = [499, 503, 500])
      fun `fail without retrying idempotent request of GET`(statusCode: Int) {
        statusCode.let { apiMockServer.stubForRetryGet("Retry $it", getPath2, 3, it, 200, body) }
        val ex = assertThrows<WebClientResponseException> { getRequestWithRetry(getPath2) }
        assertThat(ex.statusCode.value()).isEqualTo(statusCode)
        verifyApiGetPath(url = getPath2, expectedCount = 1)
      }

      @ParameterizedTest
      @ValueSource(ints = [499, 503, 500])
      fun `fail without retrying idempotent request of GET with argument`(statusCode: Int) {
        statusCode.let { apiMockServer.stubForRetryGet("Retry $it", getPath2ByIdPattern, 3, it, 200, body) }
        val ex = assertThrows<WebClientResponseException> { getRequestWithRetry(getPath2ById, "ID456") }
        assertThat(ex.statusCode.value()).isEqualTo(statusCode)
        verifyApiGetPathById(urlPattern = getPath2ByIdPattern, expectedCount = 1)
      }
    }

    @Nested
    @DisplayName("Given no response from upstream")
    @Order(3)
    inner class GivenNoResponseFromUpstream {
      private val getPath3 = "/path3"

      @Test
      fun `retry idempotent request of GET, after connection reset by peer (RST)`() {
        apiMockServer.stubForRetryGetWithFault("Retry RST", getPath3, 2, Fault.CONNECTION_RESET_BY_PEER, 200, body)
        val result = getRequestWithRetry(getPath3)
        assertTrue(result.success)
        verifyApiGetPath(url = getPath3, expectedCount = 2)
      }

      @Test
      fun `retry idempotent request of GET, after response timed out`() {
        apiMockServer.stubForRetryGetWithDelays("Retry response timed out", getPath3, 2, 151, 200, body)
        val result = getRequestWithRetry(getPath3)
        assertTrue(result.success)
        verifyApiGetPath(url = getPath3, expectedCount = 2)
      }

      @Test
      fun `retry idempotent request of GET, after connection timed out`() {
        assertThrows<UpstreamResponseException> { getRequestWithRetry(getPath3, client = unreachableWebClient) }
      }
    }

    private fun verifyApiGetPath(url: String, expectedCount: Int = 1) = apiMockServer.verify(exactly(expectedCount), getRequestedFor(urlPathEqualTo(url)))
    private fun verifyApiGetPathById(urlPattern: UrlPattern, expectedCount: Int = 1) = apiMockServer.verify(exactly(expectedCount), getRequestedFor(urlPattern))

    private fun getRequestWithRetry(uri: String, client: WebClient = webClient) = requestWithRetry(client.get().uri(uri), retrySpec(uri))
    private fun getRequestWithRetry(uri: String, vararg uriVariables: Any) = requestWithRetry(webClient.get().uri(uri, *uriVariables), retrySpec(uri))

    private fun retrySpec(uri: String) = webClientExtension.retryForIdempotentRequest(uri, upstream)
    private fun <S : RequestHeadersSpec<S>> requestWithRetry(
      requestSpec: RequestHeadersSpec<S>,
      retrySpec: Retry,
    ): TestApiResult = requestSpec
      .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
      .retrieve()
      .bodyToMono(TestApiResult::class.java)
      .retryWhen(retrySpec)
      .block()!!
  }
}

private data class TestApiResult(
  val success: Boolean,
)

private class TestWebClient(
  val baseUrl: String,
  connectTimeoutMillis: Int = 10_000,
  responseTimeoutMillis: Int = 15_000,
) {
  private val httpClient: HttpClient
  val client: WebClient

  init {
    httpClient = HttpClient.create()
      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis)
      .responseTimeout(Duration.ofMillis(responseTimeoutMillis.toLong()))

    client = WebClient.builder()
      .baseUrl(baseUrl)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .exchangeStrategies(
        ExchangeStrategies.builder()
          .codecs { configurer ->
            configurer.defaultCodecs().maxInMemorySize(-1)
          }.build(),
      ).build()
  }
}
