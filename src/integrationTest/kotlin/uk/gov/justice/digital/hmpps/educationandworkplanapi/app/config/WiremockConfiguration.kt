package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger {}

@Configuration
class WiremockConfiguration {
  companion object {
    private val loggingInitialised = AtomicBoolean(false)

    // Shared WireMock instance for all Spring contexts
    val sharedWireMockServer: WireMockServer by lazy {
      WireMockServer(
        options()
          .port(9093)
          .usingFilesUnderClasspath("simulations"),
      ).apply {
        start()
      }
    }
  }

  @Bean
  fun wireMockServer(
    @Value("\${logWiremockRequests:false}") logWiremockRequests: Boolean,
  ): WireMockServer {
    val server = sharedWireMockServer

    if (logWiremockRequests && loggingInitialised.compareAndSet(false, true)) {
      server.addMockServiceRequestListener { request: Request, _: Response ->
        val formattedHeaders = request.headers.all().joinToString("\n") {
          "${it.key()}: ${it.values().joinToString(", ")}"
        }
        val logMessage = StringBuilder()
          .appendLine("Request sent to wiremock:")
          .appendLine("${request.method} ${request.absoluteUrl}")
          .appendLine(formattedHeaders)
          .appendLine()
          .appendLine(request.bodyAsString)
        logger.info { logMessage }
      }
    }

    return server
  }
}
